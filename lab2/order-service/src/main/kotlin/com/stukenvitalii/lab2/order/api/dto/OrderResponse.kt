package com.stukenvitalii.lab2.order.api.dto

import com.stukenvitalii.lab2.order.domain.OrderStatus
import java.time.LocalDateTime

data class OrderResponse(
    val id: Long,
    val externalId: String,
    val sku: String,
    val quantity: Int,
    val itemName: String,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
)

