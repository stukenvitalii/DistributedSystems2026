package com.stukenvitalii.lab4.replication

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

private val log = KotlinLogging.logger {}

/**
 * Asynchronous message bus with simulated network delays.
 *
 * Each node has its own incoming channel. Message delivery is asynchronous:
 * a random delay in the range [minDelayMs, maxDelayMs] ms is applied before delivery.
 *
 * @param minDelayMs Minimum delivery delay (ms).
 * @param maxDelayMs Maximum delivery delay (ms).
 */
class AsyncMessageBus(
    private val minDelayMs: Long = 50L,
    private val maxDelayMs: Long = 500L,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    // nodeId -> incoming channel
    private val channels: MutableMap<String, Channel<Message>> = mutableMapOf()

    // nodeId -> message handler
    private val handlers: MutableMap<String, suspend (Message) -> Unit> = mutableMapOf()

    /**
     * Registers a node with the given incoming message handler.
     */
    fun register(nodeId: String, handler: suspend (Message) -> Unit) {
        val channel = Channel<Message>(Channel.UNLIMITED)
        channels[nodeId] = channel
        handlers[nodeId] = handler

        // Launch a receiver coroutine for this node
        scope.launch {
            for (message in channel) {
                try {
                    handler(message)
                } catch (e: Exception) {
                    log.error(e) { "[$nodeId] Error processing message: $message" }
                }
            }
        }
    }

    /**
     * Sends a message to [targetNodeId] with a simulated random network delay.
     */
    fun send(targetNodeId: String, message: Message) {
        val channel = channels[targetNodeId]
            ?: throw IllegalArgumentException("Node '$targetNodeId' is not registered on the bus.")
        val delayMs = Random.nextLong(minDelayMs, maxDelayMs + 1)
        scope.launch {
            log.debug { "  [bus] Delay ${delayMs}ms -> $targetNodeId" }
            delay(delayMs.milliseconds)
            channel.send(message)
        }
    }

    /**
     * Shuts down the bus and cancels all coroutines.
     */
    fun shutdown() {
        scope.cancel()
        channels.values.forEach { it.close() }
    }
}
