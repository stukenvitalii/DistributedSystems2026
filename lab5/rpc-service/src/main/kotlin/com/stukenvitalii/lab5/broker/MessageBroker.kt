package com.stukenvitalii.lab5.broker

import com.stukenvitalii.lab5.model.OrderEvent
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private val log = KotlinLogging.logger {}

/**
 * In-memory publish/subscribe message broker.
 *
 * Supports multiple subscribers per topic. Each published event is delivered
 * to all subscribers of that topic asynchronously (fan-out).
 *
 * No real network or persistence — all communication happens in-process via coroutines.
 */
class MessageBroker(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    // topic -> list of subscriber handlers
    private val subscribers: ConcurrentHashMap<String, CopyOnWriteArrayList<suspend (OrderEvent) -> Unit>> =
        ConcurrentHashMap()

    /**
     * Subscribes [handler] to all events published on [topic].
     *
     * Multiple handlers can be registered for the same topic (fan-out delivery).
     */
    fun subscribe(topic: String, handler: suspend (OrderEvent) -> Unit) {
        subscribers.getOrPut(topic) { CopyOnWriteArrayList() }.add(handler)
        log.info { "[broker] New subscriber registered on topic='$topic'" }
    }

    /**
     * Publishes [event] to [topic].
     *
     * Each registered subscriber is notified asynchronously in its own coroutine.
     * The publisher is not blocked waiting for delivery.
     */
    fun publish(topic: String, event: OrderEvent) {
        val handlers = subscribers[topic]
        if (handlers.isNullOrEmpty()) {
            log.warn { "[broker] No subscribers for topic='$topic', event dropped: $event" }
            return
        }
        log.info { "[broker] Publishing event to topic='$topic' (${handlers.size} subscriber(s)): orderId=${event.orderId}" }
        for (handler in handlers) {
            scope.launch {
                try {
                    handler(event)
                } catch (e: Exception) {
                    log.error(e) { "[broker] Error in subscriber handler for topic='$topic'" }
                }
            }
        }
    }

    /** Stops all coroutines managed by this broker. */
    @Suppress("unused")
    fun shutdown() {
        scope.cancel()
    }
}

