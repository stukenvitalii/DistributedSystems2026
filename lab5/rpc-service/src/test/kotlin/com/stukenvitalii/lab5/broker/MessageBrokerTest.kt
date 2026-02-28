package com.stukenvitalii.lab5.broker

import com.stukenvitalii.lab5.model.OrderEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class MessageBrokerTest {

    @Test
    fun `subscriber receives published event`() = runTest {
        val broker = MessageBroker()
        val received = mutableListOf<OrderEvent>()

        broker.subscribe("test-topic") { event -> received.add(event) }

        val event = OrderEvent(orderId = "ORD-1", item = "Pen", quantity = 5)
        broker.publish("test-topic", event)

        // Give the async delivery coroutine time to execute
        delay(100.milliseconds)

        assertEquals(1, received.size)
        assertEquals("ORD-1", received.first().orderId)
    }

    @Test
    fun `multiple subscribers all receive the same event (fan-out)`() = runTest {
        val broker = MessageBroker()
        val bucket1 = mutableListOf<String>()
        val bucket2 = mutableListOf<String>()
        val bucket3 = mutableListOf<String>()

        broker.subscribe("orders") { bucket1.add(it.orderId) }
        broker.subscribe("orders") { bucket2.add(it.orderId) }
        broker.subscribe("orders") { bucket3.add(it.orderId) }

        broker.publish("orders", OrderEvent("ORD-42", "Book", 1))
        delay(100.milliseconds)

        assertEquals(listOf("ORD-42"), bucket1)
        assertEquals(listOf("ORD-42"), bucket2)
        assertEquals(listOf("ORD-42"), bucket3)
    }

    @Test
    fun `events on different topics are isolated`() = runTest {
        val broker = MessageBroker()
        val topicA = mutableListOf<String>()
        val topicB = mutableListOf<String>()

        broker.subscribe("topic-A") { topicA.add(it.orderId) }
        broker.subscribe("topic-B") { topicB.add(it.orderId) }

        broker.publish("topic-A", OrderEvent("A-1", "X", 1))
        broker.publish("topic-B", OrderEvent("B-1", "Y", 1))
        delay(100.milliseconds)

        assertEquals(listOf("A-1"), topicA)
        assertEquals(listOf("B-1"), topicB)
    }

    @Test
    fun `publish to topic with no subscribers does not throw`() = runTest {
        val broker = MessageBroker()
        // Should log a warning and return silently
        broker.publish("ghost-topic", OrderEvent("ORD-0", "Nothing", 0))
    }
}

