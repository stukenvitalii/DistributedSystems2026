package com.stukenvitalii.lab3.auth.domain

data class StoredUser(
    val username: String,
    val passwordHash: String?,
    val roles: Set<String>
)

