package com.stukenvitalii.lab2.order.service.exceptions

import org.springframework.http.HttpStatusCode

class InventoryServiceException(
    val status: HttpStatusCode,
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
