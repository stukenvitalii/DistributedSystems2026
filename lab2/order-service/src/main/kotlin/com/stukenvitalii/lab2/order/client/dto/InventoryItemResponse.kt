package com.stukenvitalii.lab2.order.client.dto

data class InventoryItemResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val quantity: Int,
)

