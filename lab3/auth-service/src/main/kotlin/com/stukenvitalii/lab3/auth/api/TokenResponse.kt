package com.stukenvitalii.lab3.auth.api

import java.time.Instant

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val issuedAt: Instant,
    val expiresAt: Instant
)

