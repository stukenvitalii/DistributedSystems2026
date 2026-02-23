package com.stukenvitalii.lab2.order.service

import com.stukenvitalii.lab2.order.api.dto.CreateOrderRequest
import com.stukenvitalii.lab2.order.api.dto.OrderResponse
import com.stukenvitalii.lab2.order.client.InventoryClient
import com.stukenvitalii.lab2.order.domain.Order
import com.stukenvitalii.lab2.order.domain.OrderRepository
import com.stukenvitalii.lab2.order.service.exceptions.OrderAlreadyExistsException
import com.stukenvitalii.lab2.order.service.exceptions.OrderNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val inventoryClient: InventoryClient,
) {

    @Transactional
    fun create(request: CreateOrderRequest): OrderResponse {
        if (orderRepository.existsByExternalId(request.externalId)) {
            throw OrderAlreadyExistsException(request.externalId)
        }
        val inventoryItem = inventoryClient.reserveStock(request.sku, request.quantity)
        val saved = orderRepository.save(
            Order(
                externalId = request.externalId,
                sku = request.sku,
                quantity = request.quantity,
                itemName = inventoryItem.name,
            )
        )
        return saved.toResponse()
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): OrderResponse = findOrder(id).toResponse()

    @Transactional(readOnly = true)
    fun listAll(): List<OrderResponse> = orderRepository.findAll()
        .sortedByDescending { it.createdAt }
        .map { it.toResponse() }

    private fun findOrder(id: Long): Order =
        orderRepository.findById(id).orElseThrow { OrderNotFoundException(id) }

    private fun Order.toResponse(): OrderResponse =
        OrderResponse(
            id = requireNotNull(id),
            externalId = externalId,
            sku = sku,
            quantity = quantity,
            itemName = itemName,
            status = status,
            createdAt = createdAt,
        )
}

