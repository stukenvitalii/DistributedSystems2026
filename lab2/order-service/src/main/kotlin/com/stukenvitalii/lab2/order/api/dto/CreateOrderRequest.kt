package com.stukenvitalii.lab2.order.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

class CreateOrderRequest(
    @field:NotBlank
    val externalId: String,
    @field:NotBlank
    val sku: String,
    @field:Min(1)
    val quantity: Int,
)

