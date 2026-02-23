package com.stukenvitalii.lab2.order.api

import com.stukenvitalii.lab2.order.api.dto.CreateOrderRequest
import com.stukenvitalii.lab2.order.api.dto.OrderResponse
import com.stukenvitalii.lab2.order.service.OrderService
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
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid request: CreateOrderRequest): OrderResponse =
        orderService.create(request)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): OrderResponse = orderService.getById(id)

    @GetMapping
    fun listAll(): List<OrderResponse> = orderService.listAll()
}

