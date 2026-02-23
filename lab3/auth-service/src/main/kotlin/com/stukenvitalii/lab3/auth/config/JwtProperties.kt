package com.stukenvitalii.lab3.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperties(
    var secret: String = "",
    var issuer: String = "auth-service",
    var accessTokenTtlSeconds: Long = 600
)

