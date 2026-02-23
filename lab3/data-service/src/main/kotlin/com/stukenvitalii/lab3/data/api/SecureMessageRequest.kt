package com.stukenvitalii.lab3.data.api

import jakarta.validation.constraints.NotBlank

data class SecureMessageRequest(
    @field:NotBlank val payload: String
)

