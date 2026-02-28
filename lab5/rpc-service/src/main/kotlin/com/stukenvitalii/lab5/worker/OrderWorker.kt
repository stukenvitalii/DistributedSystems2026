package com.stukenvitalii.lab5.worker

import com.stukenvitalii.lab5.broker.MessageBroker
import com.stukenvitalii.lab5.model.OrderEvent
import com.stukenvitalii.lab5.model.OrderStatus
import com.stukenvitalii.lab5.service.ORDERS_TOPIC
import com.stukenvitalii.lab5.service.OrderService
import kotlinx.coroutines.delay
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val log = KotlinLogging.logger {}

/**
 * Order processing worker (pub/sub consumer).
 *
 * Subscribes to [ORDERS_TOPIC] on the provided [broker].
 * When an [OrderEvent] arrives the worker transitions the order through its
 * full lifecycle: ACCEPTED -> PROCESSING -> COMPLETED, updating the service
 * status at each step.
 *
 * Multiple [OrderWorker] instances can be registered on the same broker to
 * demonstrate fan-out delivery: every worker receives every event.
 *
 * @param broker         Message broker to subscribe to.
 * @param service        Order service used to update the order status.
 * @param workerId       Human-readable identifier for log output.
 * @param processingTime How long the simulated job takes (default 2 s).
 */
@Suppress("unused")
class OrderWorker(
    private val broker: MessageBroker,
    private val service: OrderService,
    val workerId: String,
    private val processingTime: Duration = 2000.milliseconds
) {
    init {
        broker.subscribe(ORDERS_TOPIC) { event -> handleEvent(event) }
        log.info { "[$workerId] Started, subscribed to topic='$ORDERS_TOPIC'" }
    }

    private suspend fun handleEvent(event: OrderEvent) {
        log.info {
            "[$workerId] Received event: orderId='${event.orderId}' " +
                    "item='${event.item}' qty=${event.quantity}"
        }

        // Transition: ACCEPTED -> PROCESSING
        service.updateStatus(event.orderId, OrderStatus.PROCESSING)
        log.info { "[$workerId] orderId='${event.orderId}' -> PROCESSING (working for $processingTime)" }

        // Simulate time-consuming work (payment gateway, inventory reservation, etc.)
        delay(processingTime)

        // Transition: PROCESSING -> COMPLETED
        service.updateStatus(event.orderId, OrderStatus.COMPLETED)
        log.info { "[$workerId] orderId='${event.orderId}' -> COMPLETED" }
    }
}
