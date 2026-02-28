package com.stukenvitalii.lab5.client

import com.stukenvitalii.lab5.broker.MessageBroker
import com.stukenvitalii.lab5.model.OrderRequest
import com.stukenvitalii.lab5.service.OrderService
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class RpcClientTest {

    private fun setup(failureProbability: Double = 0.0, maxRetries: Int = 3): Pair<RpcClient, OrderService> {
        val service = OrderService(MessageBroker())
        val client = RpcClient(
            service = service,
            timeout = 500.milliseconds,
            maxRetries = maxRetries,
            retryDelay = 10.milliseconds,
            failureProbability = failureProbability
        )
        return client to service
    }

    @Test
    fun `successful call with no failures creates order`() = runTest {
        val (client, service) = setup(failureProbability = 0.0)
        val resp = client.postOrder(OrderRequest("req-ok", "Widget", 1))
        assertFalse(resp.alreadyExisted)
        assertEquals(1, service.allOrders().size)
    }

    @Test
    fun `idempotent retry does not create duplicate order`() = runTest {
        val (client, service) = setup(failureProbability = 0.0)
        val req = OrderRequest("req-idem", "Gadget", 2)
        val r1 = client.postOrder(req)
        val r2 = client.postOrder(req)
        assertEquals(r1.orderId, r2.orderId)
        assertTrue(r2.alreadyExisted)
        assertEquals(1, service.allOrders().size)
    }

    @Test
    fun `always-failing client exhausts retries and throws RpcException`() = runTest {
        val (client, _) = setup(failureProbability = 1.0, maxRetries = 2)
        assertFailsWith<RpcException> {
            client.postOrder(OrderRequest("req-fail", "Brick", 1))
        }
    }

    @Test
    fun `exhausted retries leave no order in service`() = runTest {
        val (client, service) = setup(failureProbability = 1.0, maxRetries = 1)
        runCatching { client.postOrder(OrderRequest("req-noop", "Air", 0)) }
        // Service never gets called because failure is injected before the call
        assertEquals(0, service.allOrders().size)
    }
}

