package com.stukenvitalii.lab2.inventory.api

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)

