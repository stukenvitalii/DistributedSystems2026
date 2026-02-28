package com.stukenvitalii.lab5.service

import com.stukenvitalii.lab5.broker.MessageBroker
import com.stukenvitalii.lab5.model.OrderEvent
import com.stukenvitalii.lab5.model.OrderRequest
import com.stukenvitalii.lab5.model.OrderResponse
import com.stukenvitalii.lab5.model.OrderStatus
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val log = KotlinLogging.logger {}

/** Topic name used for order events. */
const val ORDERS_TOPIC = "orders"

/**
 * RPC Order Service.
 *
 * Exposes a single operation [createOrder] that corresponds to `POST /orders`.
 *
 * ## Idempotency
 * Every request carries a client-generated [OrderRequest.requestId].
 * If the server has already processed a request with the same ID it returns
 * the cached response immediately, guaranteeing exactly-once semantics even
 * when the client retries after a timeout or network loss.
 *
 * ## Pub/Sub integration
 * After successfully creating a new order the service publishes an [OrderEvent]
 * to [ORDERS_TOPIC] via the injected [MessageBroker].
 *
 * @param broker Message broker used to publish order events.
 */
class OrderService(private val broker: MessageBroker) {

    // Idempotency cache: requestId -> previously returned response
    private val idempotencyCache: ConcurrentHashMap<String, OrderResponse> = ConcurrentHashMap()

    // Order store: orderId -> pair of (request, current response/status)
    private val orders: ConcurrentHashMap<String, Pair<OrderRequest, OrderResponse>> = ConcurrentHashMap()

    /**
     * Creates a new order or returns the cached response if [request.requestId] was seen before.
     *
     * Simulates a small amount of server-side processing time.
     *
     * @param request Incoming order request.
     * @return [OrderResponse] with a stable orderId regardless of how many times
     *         the same requestId is submitted.
     */
    fun createOrder(request: OrderRequest): OrderResponse {
        // --- Idempotency check ---
        idempotencyCache[request.requestId]?.let { cached ->
            log.info {
                "[service] Duplicate request detected: requestId='${request.requestId}' " +
                        "-> returning cached response (orderId='${cached.orderId}')"
            }
            return cached.copy(alreadyExisted = true)
        }

        // --- Create new order ---
        val orderId = UUID.randomUUID().toString().take(8).uppercase()
        val response = OrderResponse(orderId = orderId, status = OrderStatus.ACCEPTED)

        orders[orderId] = request to response
        idempotencyCache[request.requestId] = response

        log.info {
            "[service] Order CREATED: orderId='$orderId' item='${request.item}' " +
                    "qty=${request.quantity} requestId='${request.requestId}'"
        }

        // --- Publish event to broker ---
        val event = OrderEvent(orderId = orderId, item = request.item, quantity = request.quantity)
        broker.publish(ORDERS_TOPIC, event)

        return response
    }

    /**
     * Updates the status of an existing order.
     *
     * Called by [com.stukenvitalii.lab5.worker.OrderWorker] to transition the order
     * through its lifecycle: ACCEPTED -> PROCESSING -> COMPLETED.
     *
     * @param orderId   The order to update.
     * @param newStatus The new status to apply.
     */
    fun updateStatus(orderId: String, newStatus: OrderStatus) {
        orders.computeIfPresent(orderId) { _, (req, resp) ->
            val updated = resp.copy(status = newStatus)
            log.info { "[service] Order status updated: orderId='$orderId' ${resp.status} -> $newStatus" }
            req to updated
        }
    }

    /** Returns the current status of an order, or null if not found. */
    fun getStatus(orderId: String): OrderStatus? = orders[orderId]?.second?.status

    /** Returns a snapshot of all stored order requests (for diagnostics). */
    fun allOrders(): Map<String, OrderRequest> = orders.mapValues { it.value.first }
}
