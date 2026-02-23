package com.stukenvitalii.lab3.auth.repository

import com.stukenvitalii.lab3.auth.domain.StoredUser
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CredentialStore(passwordEncoder: PasswordEncoder) {

    private val users: Map<String, StoredUser>

    init {
        val seedUsers = listOf(
            StoredUser("alice", passwordEncoder.encode("alicePass123"), setOf("ROLE_USER")),
            StoredUser("bob", passwordEncoder.encode("bobService!"), setOf("ROLE_USER", "ROLE_ADMIN")),
            StoredUser("service-client", passwordEncoder.encode("svc-client-key"), setOf("ROLE_SERVICE"))
        )
        users = seedUsers.associateBy { it.username }
    }

    fun findByUsername(username: String): StoredUser? = users[username]
}

