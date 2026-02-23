package com.stukenvitalii.lab3.auth.service

import com.stukenvitalii.lab3.auth.api.LoginRequest
import com.stukenvitalii.lab3.auth.api.TokenResponse
import com.stukenvitalii.lab3.auth.repository.CredentialStore
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val credentialStore: CredentialStore,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun login(request: LoginRequest): TokenResponse {
        val user = credentialStore.findByUsername(request.username)
            ?: throw BadCredentialsException("Unknown username")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            logger.warn("Failed login attempt for user {}", request.username)
            throw BadCredentialsException("Invalid credentials")
        }

        logger.info("Successful login for user {}", request.username)
        return tokenService.issueToken(user.username, user.roles)
    }
}

