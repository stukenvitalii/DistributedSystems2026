package com.stukenvitalii.lab2.inventory.service

import com.stukenvitalii.lab2.inventory.api.dto.CreateItemRequest
import com.stukenvitalii.lab2.inventory.api.dto.ItemResponse
import com.stukenvitalii.lab2.inventory.api.dto.ReserveStockRequest
import com.stukenvitalii.lab2.inventory.domain.Item
import com.stukenvitalii.lab2.inventory.domain.ItemRepository
import com.stukenvitalii.lab2.inventory.service.exceptions.InsufficientStockException
import com.stukenvitalii.lab2.inventory.service.exceptions.ItemAlreadyExistsException
import com.stukenvitalii.lab2.inventory.service.exceptions.ItemNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryService(
    private val itemRepository: ItemRepository,
) {

    @Transactional
    fun create(request: CreateItemRequest): ItemResponse {
        if (itemRepository.existsBySku(request.sku)) {
            throw ItemAlreadyExistsException(request.sku)
        }
        val saved = itemRepository.save(
            Item(
                sku = request.sku,
                name = request.name,
                quantity = request.quantity,
            )
        )
        return saved.toResponse()
    }

    @Transactional(readOnly = true)
    fun getBySku(sku: String): ItemResponse = findItem(sku).toResponse()

    @Transactional
    fun reserve(request: ReserveStockRequest): ItemResponse {
        val item = findItem(request.sku)
        if (item.quantity < request.quantity) {
            throw InsufficientStockException(request.sku)
        }
        item.quantity -= request.quantity
        return item.toResponse()
    }

    private fun findItem(sku: String): Item =
        itemRepository.findBySku(sku) ?: throw ItemNotFoundException(sku)

    private fun Item.toResponse(): ItemResponse =
        ItemResponse(
            id = requireNotNull(id),
            sku = sku,
            name = name,
            quantity = quantity,
        )
}

