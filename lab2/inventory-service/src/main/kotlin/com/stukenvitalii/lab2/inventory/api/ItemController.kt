package com.stukenvitalii.lab2.inventory.api

import com.stukenvitalii.lab2.inventory.api.dto.CreateItemRequest
import com.stukenvitalii.lab2.inventory.api.dto.ItemResponse
import com.stukenvitalii.lab2.inventory.api.dto.ReserveStockRequest
import com.stukenvitalii.lab2.inventory.service.InventoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ItemController(
    private val inventoryService: InventoryService,
) {

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid request: CreateItemRequest): ItemResponse =
        inventoryService.create(request)

    @GetMapping("/items/{sku}")
    fun getBySku(@PathVariable sku: String): ItemResponse = inventoryService.getBySku(sku)

    @PostMapping("/reservations")
    fun reserve(@RequestBody @Valid request: ReserveStockRequest): ItemResponse =
        inventoryService.reserve(request)
}

