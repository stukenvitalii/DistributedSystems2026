package com.stukenvitalii.lab2.inventory

import com.stukenvitalii.lab2.inventory.api.dto.CreateItemRequest
import com.stukenvitalii.lab2.inventory.api.dto.ReserveStockRequest
import com.stukenvitalii.lab2.inventory.service.InventoryService
import org.junit.jupiter.api.Assertions.assertEquals
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
class InventoryServiceApplicationTests {

    @Autowired
    private lateinit var inventoryService: InventoryService

    @Test
    fun `creates item and reserves it`() {
        val created = inventoryService.create(CreateItemRequest("book-001", "Book", 10))
        val reserved = inventoryService.reserve(ReserveStockRequest("book-001", 3))
        assertEquals(created.id, reserved.id)
        assertEquals(7, reserved.quantity)
    }

    companion object {
        @Container
        private val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("inventory_test")
            withUsername("inventory")
            withPassword("inventory")
        }

        @JvmStatic
        @DynamicPropertySource
        fun datasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }
}

