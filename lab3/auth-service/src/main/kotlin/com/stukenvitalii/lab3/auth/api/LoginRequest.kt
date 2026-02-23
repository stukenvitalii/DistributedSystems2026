package com.stukenvitalii.lab3.auth.api

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "username is required")
    val username: String,
    @field:NotBlank(message = "password is required")
    val password: String
)

