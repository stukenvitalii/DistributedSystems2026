package com.stukenvitalii.lab3.data.api

import java.time.Instant

data class SecureMessageResponse(
    val receivedAt: Instant,
    val echoedPayload: String,
    val processedBy: String
)

