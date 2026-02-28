package com.stukenvitalii.lab5.service

import com.stukenvitalii.lab5.broker.MessageBroker
import com.stukenvitalii.lab5.model.OrderRequest
import com.stukenvitalii.lab5.model.OrderStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class OrderServiceTest {

    private fun service() = OrderService(MessageBroker())

    @Test
    fun `createOrder returns ACCEPTED status`() = runTest {
        val svc = service()
        val resp = svc.createOrder(OrderRequest("req-1", "Laptop", 1))
        assertEquals(OrderStatus.ACCEPTED, resp.status)
        assertFalse(resp.alreadyExisted)
        assertNotNull(resp.orderId)
    }

    @Test
    fun `duplicate requestId returns cached response with alreadyExisted=true`() = runTest {
        val svc = service()
        val req = OrderRequest("req-dup", "Phone", 2)

        val first = svc.createOrder(req)
        val second = svc.createOrder(req)
        val third = svc.createOrder(req)

        assertFalse(first.alreadyExisted)
        assertTrue(second.alreadyExisted)
        assertTrue(third.alreadyExisted)

        // All three calls must return the same orderId
        assertEquals(first.orderId, second.orderId)
        assertEquals(first.orderId, third.orderId)
    }

    @Test
    fun `different requestIds create separate orders`() = runTest {
        val svc = service()
        val r1 = svc.createOrder(OrderRequest("req-A", "Tablet", 1))
        val r2 = svc.createOrder(OrderRequest("req-B", "Tablet", 1))

        assertFalse(r1.alreadyExisted)
        assertFalse(r2.alreadyExisted)
        assertTrue(r1.orderId != r2.orderId)
        assertEquals(2, svc.allOrders().size)
    }

    @Test
    fun `only one order is stored regardless of duplicate requests`() = runTest {
        val svc = service()
        val req = OrderRequest("req-single", "Chair", 4)
        repeat(5) { svc.createOrder(req) }
        assertEquals(1, svc.allOrders().size)
    }
}

