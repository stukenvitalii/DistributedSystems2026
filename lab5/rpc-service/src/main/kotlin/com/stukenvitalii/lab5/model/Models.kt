package com.stukenvitalii.lab5.model

/**
 * Request to create an order, sent by the client to the RPC service.
 *
 * @property requestId Unique client-generated ID used for idempotency detection.
 * @property item      Name of the item being ordered.
 * @property quantity  Quantity of the item.
 */
data class OrderRequest(
    val requestId: String,
    val item: String,
    val quantity: Int
)

/**
 * Response returned by the RPC service after processing an order request.
 *
 * @property orderId       Server-assigned order identifier.
 * @property status        Current order status.
 * @property alreadyExisted True if this request was a duplicate (idempotent replay).
 */
data class OrderResponse(
    val orderId: String,
    val status: OrderStatus,
    val alreadyExisted: Boolean = false
)

/**
 * Lifecycle status of an order.
 */
enum class OrderStatus {
    /** Order has been accepted and is queued for processing. */
    ACCEPTED,

    /** Order is currently being processed by a worker. */
    PROCESSING,

    /** Order has been fully processed. */
    COMPLETED
}

/**
 * Domain event published to the message broker after an order is created.
 *
 * @property orderId   The created order's ID.
 * @property item      Item name.
 * @property quantity  Item quantity.
 * @property timestamp Wall-clock time when the event was published (ms).
 */
data class OrderEvent(
    val orderId: String,
    val item: String,
    val quantity: Int,
    val timestamp: Long = System.currentTimeMillis()
)
