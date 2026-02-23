package com.stukenvitalii.lab2.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.stukenvitalii.lab2.order.api.dto.CreateOrderRequest
import com.stukenvitalii.lab2.order.service.OrderService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class OrderServiceApplicationTests {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `creates order when inventory confirms reservation`() {
        val inventoryResponse = mapOf(
            "id" to 1,
            "sku" to "book-001",
            "name" to "Book",
            "quantity" to 7
        )
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(inventoryResponse))
                .addHeader("Content-Type", "application/json")
        )

        val order = orderService.create(CreateOrderRequest("ext-001", "book-001", 2))

        assertEquals("book-001", order.sku)
        assertEquals(2, order.quantity)
        assertEquals("Book", order.itemName)
    }

    companion object {
        @Container
        private val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("order_test")
            withUsername("orders")
            withPassword("orders")
        }

        private val mockWebServer = MockWebServer()

        @JvmStatic
        @BeforeAll
        fun startServer() {
            mockWebServer.start()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            mockWebServer.shutdown()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configure(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("inventory.client.base-url") { mockWebServer.url("/").toString().trimEnd('/') }
        }
    }
}

