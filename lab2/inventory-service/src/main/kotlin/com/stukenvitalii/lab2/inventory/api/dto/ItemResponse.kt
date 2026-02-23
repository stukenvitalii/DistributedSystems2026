package com.stukenvitalii.lab2.inventory.api.dto

data class ItemResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val quantity: Int,
)

