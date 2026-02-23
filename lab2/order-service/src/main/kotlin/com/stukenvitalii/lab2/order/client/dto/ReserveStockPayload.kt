package com.stukenvitalii.lab2.order.client.dto

data class ReserveStockPayload(
    val sku: String,
    val quantity: Int,
)

