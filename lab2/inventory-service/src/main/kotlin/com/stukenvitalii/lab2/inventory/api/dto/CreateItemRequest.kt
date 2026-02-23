package com.stukenvitalii.lab2.inventory.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

class CreateItemRequest(
    @field:NotBlank
    val sku: String,
    @field:NotBlank
    val name: String,
    @field:Min(0)
    val quantity: Int,
)

