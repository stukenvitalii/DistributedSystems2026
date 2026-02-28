# Lab 5 — Fault Tolerance and Security in Distributed Systems (RPC Service)

## Goal

Design a system containing an RPC service with simulated failures and a message broker.

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                          System Architecture                         │
│                                                                      │
│   ┌────────────┐   POST /orders    ┌──────────────────────────────┐  │
│   │  RpcClient │ ──────────────>  │       OrderService            │  │
│   │            │   (requestId)    │  idempotency cache            │  │
│   │ - timeout  │ <──────────────  │  ConcurrentHashMap<reqId,..>  │  │
│   │ - retry    │   OrderResponse  └──────────────┬────────────────┘  │
│   └────────────┘                                 │ publish event     │
│                                                  ▼                   │
│                                     ┌────────────────────┐           │
│                                     │   MessageBroker    │           │
│                                     │  topic: "orders"   │           │
│                                     └──────┬──────┬──────┘           │
│                                            │      │  fan-out         │
│                                     ┌──────┘      └───────┐          │
│                                     ▼                     ▼          │
│                              ┌────────────┐       ┌────────────┐     │
│                              │  Worker-1  │       │  Worker-2  │     │
│                              │ (2s delay) │       │ (3s delay) │     │
│                              └────────────┘       └────────────┘     │
└──────────────────────────────────────────────────────────────────────┘
```

## Components

| Component       | Description                                                                                                                                           |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| `OrderService`  | RPC endpoint `POST /orders`. Creates orders and caches responses by `requestId` for idempotency. Publishes `OrderEvent` to the broker after creation. |
| `RpcClient`     | Simulated RPC client with configurable `timeout`, `maxRetries`, `retryDelay`, and `failureProbability`. Wraps each attempt in `withTimeout`.          |
| `MessageBroker` | In-memory pub/sub broker. Supports multiple subscribers per topic (fan-out). Delivery is asynchronous via coroutines.                                 |
| `OrderWorker`   | Pub/sub consumer. Subscribes to `"orders"` topic, simulates long-running processing (`delay`).                                                        |

## Key Mechanisms

### 1. Idempotency (exactly-once semantics)

The service keeps a `ConcurrentHashMap<requestId, OrderResponse>`. On every incoming
request it checks the cache first:

- **Cache hit** → return the same `OrderResponse` with `alreadyExisted = true`. No new order is created.
- **Cache miss** → create order, store in cache, publish event, return response.

This makes client retries safe: re-sending the same `requestId` never produces duplicates.

### 2. Retry with Timeout

```
attempt 1 ──withTimeout──> fail/timeout ──delay──> attempt 2 ──> ... ──> success
                                                                    └──> RpcException (retries exhausted)
```

Each attempt is wrapped in `withTimeout(duration)`. On `TimeoutCancellationException` or
`SimulatedNetworkException` the client waits `retryDelay` and tries again.

### 3. Pub/Sub Message Broker

```
publisher.publish("orders", event)
    └──> coroutine for subscriber-1 (OrderWorker-1)
    └──> coroutine for subscriber-2 (OrderWorker-2)
```

The broker spawns one coroutine per subscriber per published message. The publisher
returns immediately — fully non-blocking.

## Demonstration Scenarios

| # | Scenario                                  | Key observation                                                  |
|---|-------------------------------------------|------------------------------------------------------------------|
| 1 | Same `requestId` sent 3 times             | Only 1 order created; calls 2 & 3 return `alreadyExisted=true`   |
| 2 | `failureProbability=0.65`, `maxRetries=6` | Client retries until success; logs show failed attempts          |
| 3 | `failureProbability=1.0`, `maxRetries=2`  | All 3 attempts fail → `RpcException` thrown                      |
| 4 | Two workers subscribed                    | Both workers receive every event (fan-out); process concurrently |

## Build & Run

```bash
# Build fat-jar
cd labs/lab5/rpc-service
mvn package

# Run
java -jar target/rpc-service-0.0.1-SNAPSHOT-all.jar
```

Run with full logs:

```bash
java -jar target/rpc-service-0.0.1-SNAPSHOT-all.jar
```

Run with only WARNING+ logs (cleaner output):

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=warn -jar target/rpc-service-0.0.1-SNAPSHOT-all.jar
```

## Tests

```bash
cd labs/lab5/rpc-service
mvn test
```

Test coverage (12 tests total):

| Test class          | Tests                                                                                                                                                        |
|---------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `OrderServiceTest`  | createOrder returns ACCEPTED; duplicate requestId returns cached response; different requestIds create separate orders; only one order stored for duplicates |
| `RpcClientTest`     | Successful call; idempotent retry no duplicate; always-failing throws RpcException; exhausted retries leave no order                                         |
| `MessageBrokerTest` | Subscriber receives event; multiple subscribers get same event (fan-out); topics are isolated; publish to empty topic does not throw                         |

## Conclusions

1. **Idempotency** is a server-side concern. The client should always include a stable
   `requestId` so that retries are safe without any coordination.

2. **Retry + Timeout** at the client level handles transient failures gracefully.
   Without server-side idempotency, retries would cause duplicate side-effects.

3. **Exhausted retries** must be surfaced as a typed exception. The caller decides
   whether to retry later, alert the user, or compensate.

4. **Pub/Sub decoupling** lets the order service remain unaware of downstream consumers.
   Workers can be added, removed, or scaled independently. Fan-out ensures every
   subscriber gets every event.

