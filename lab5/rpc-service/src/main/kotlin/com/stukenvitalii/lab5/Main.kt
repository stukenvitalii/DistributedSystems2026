package com.stukenvitalii.lab5

import com.stukenvitalii.lab5.broker.MessageBroker
import com.stukenvitalii.lab5.client.RpcClient
import com.stukenvitalii.lab5.client.RpcException
import com.stukenvitalii.lab5.model.OrderRequest
import com.stukenvitalii.lab5.service.OrderService
import com.stukenvitalii.lab5.worker.OrderWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Entry point — demonstration scenarios for Lab 5.
 *
 * ┌────────────────────────────────────────────────────────────────┐
 * │                       System Architecture                      │
 * │                                                                │
 * │   RpcClient  ──POST /orders──>  OrderService                  │
 * │   (timeout + retry)             (idempotency cache)           │
 * │                                       │                       │
 * │                                  MessageBroker                │
 * │                                  topic: "orders"              │
 * │                                       │                       │
 * │                                  OrderWorker-1                │
 * │                                  OrderWorker-2  (fan-out)     │
 * └────────────────────────────────────────────────────────────────┘
 *
 * Scenario 1 — Idempotency:
 *   The same requestId is sent 3 times. Only one order is created;
 *   subsequent calls return the cached response with alreadyExisted=true.
 *
 * Scenario 2 — Retry on transient failures:
 *   Client is configured with failureProbability=0.65. The same order
 *   request is retried until it succeeds (or retries are exhausted).
 *
 * Scenario 3 — Exhausted retries:
 *   Client is configured with failureProbability=1.0 (always fails).
 *   After maxRetries attempts an RpcException is thrown.
 *
 * Scenario 4 — Pub/Sub worker processing:
 *   Two workers are subscribed; both receive and process every order event
 *   asynchronously, demonstrating fan-out delivery.
 */
fun main() = runBlocking {
    printBanner()

    val broker = MessageBroker()
    val service = OrderService(broker)

    // Register two workers for fan-out demonstration.
    // Workers self-register in their init block — no need to hold a variable reference.
    OrderWorker(broker, service, workerId = "Worker-1", processingTime = 2.seconds)
    OrderWorker(broker, service, workerId = "Worker-2", processingTime = 3.seconds)

    // =========================================================================
    // Scenario 1: Idempotency — same requestId sent multiple times
    // =========================================================================
    section("SCENARIO 1: Idempotency — duplicate requestId detection")

    val reliableClient = RpcClient(service, failureProbability = 0.0, maxRetries = 0)
    val req1 = OrderRequest(requestId = "req-AAA", item = "Laptop", quantity = 1)

    println("\n>> Sending the same request 3 times with requestId='${req1.requestId}'")
    repeat(3) { i ->
        val resp = reliableClient.postOrder(req1)
        println("   [call ${i + 1}] orderId='${resp.orderId}' alreadyExisted=${resp.alreadyExisted}")
    }

    println("\n>> Total orders in service: ${service.allOrders().size}  (expected: 1)")

    // =========================================================================
    // Scenario 2: Retry — transient failures with eventual success
    // =========================================================================
    section("SCENARIO 2: Retry — transient failures, failureProbability=0.65")

    val retryClient = RpcClient(
        service,
        timeout = 800.milliseconds,
        maxRetries = 6,
        retryDelay = 200.milliseconds,
        failureProbability = 0.65
    )

    println("\n>> Sending order for 'Keyboard' (may fail several times before success)...")
    try {
        val resp = retryClient.postOrder(OrderRequest("req-BBB", "Keyboard", 2))
        println("\n   Final result: orderId='${resp.orderId}' status=${resp.status}")
    } catch (e: RpcException) {
        println("\n   All retries exhausted: ${e.message}")
    }

    println("\n>> Sending order for 'Mouse' (another independent request)...")
    try {
        val resp = retryClient.postOrder(OrderRequest("req-CCC", "Mouse", 1))
        println("\n   Final result: orderId='${resp.orderId}' status=${resp.status}")
    } catch (e: RpcException) {
        println("\n   All retries exhausted: ${e.message}")
    }

    // =========================================================================
    // Scenario 3: Exhausted retries — always fails
    // =========================================================================
    section("SCENARIO 3: Exhausted retries — failureProbability=1.0")

    val alwaysFailClient = RpcClient(
        service,
        timeout = 500.milliseconds,
        maxRetries = 2,
        retryDelay = 100.milliseconds,
        failureProbability = 1.0
    )

    println("\n>> Sending order that will always fail (failureProbability=1.0, maxRetries=2)...")
    try {
        alwaysFailClient.postOrder(OrderRequest("req-DDD", "Monitor", 3))
    } catch (e: RpcException) {
        println("\n   [expected] RpcException caught: ${e.message}")
    }

    // =========================================================================
    // Scenario 4: Pub/Sub — wait for workers to finish processing
    // =========================================================================
    section("SCENARIO 4: Pub/Sub fan-out — waiting for workers to complete")

    println("\n>> All previously created orders have already triggered worker events.")
    println(">> Sending one more order to observe both workers processing it concurrently...")

    val freshClient = RpcClient(service, failureProbability = 0.0)
    val resp4 = freshClient.postOrder(OrderRequest("req-EEE", "Headphones", 1))
    println("\n   Order created: orderId='${resp4.orderId}'")

    println("\n>> Waiting up to 4s for workers to finish...")
    delay(4.seconds)

    // =========================================================================
    // Summary
    // =========================================================================
    println()
    println("=".repeat(70))
    println("  FINAL STATE")
    println("=".repeat(70))
    println("  Total orders created: ${service.allOrders().size}")
    service.allOrders().forEach { (id, req) ->
        val status = service.getStatus(id)
        println("  - orderId='$id'  item='${req.item}'  qty=${req.quantity}  status=$status")
    }

    println()
    println("=".repeat(70))
    println("  CONCLUSIONS")
    println("=".repeat(70))
    println(
        """
  1. IDEMPOTENCY: The server uses a requestId cache to detect duplicate
     requests. Regardless of how many times the client retries, only one
     order is created. This is essential when networks are unreliable.

  2. RETRY WITH TIMEOUT: The client wraps each attempt in withTimeout()
     and retries on failure/timeout. Combined with server-side idempotency,
     retrying the same requestId is always safe — no duplicate orders.

  3. EXHAUSTED RETRIES: When all attempts fail the client throws RpcException.
     The caller can decide to surface the error or take compensating action.

  4. PUB/SUB BROKER: After order creation the service publishes an event.
     Workers receive it asynchronously (fan-out) and process independently.
     The service is fully decoupled from worker logic — new workers can be
     added without touching the service code.
    """.trimIndent()
    )
    println("=".repeat(70))
}

private fun section(title: String) {
    println()
    println("=".repeat(70))
    println("  $title")
    println("=".repeat(70))
}

private fun printBanner() {
    println()
    println("=".repeat(70))
    println("  Lab 5 - Fault Tolerance and Security in Distributed Systems")
    println("  Mini RPC Service | Idempotency | Retry | Pub/Sub Broker")
    println("=".repeat(70))
}

