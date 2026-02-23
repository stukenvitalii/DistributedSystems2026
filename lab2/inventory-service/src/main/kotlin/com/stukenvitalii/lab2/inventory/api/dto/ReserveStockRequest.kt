package com.stukenvitalii.lab2.inventory.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

class ReserveStockRequest(
    @field:NotBlank
    val sku: String,
    @field:Min(1)
    val quantity: Int,
)

