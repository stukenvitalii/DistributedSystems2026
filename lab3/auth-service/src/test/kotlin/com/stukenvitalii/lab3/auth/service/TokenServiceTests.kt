package com.stukenvitalii.lab3.auth.service

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.stukenvitalii.lab3.auth.config.JwtProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.spec.SecretKeySpec

class TokenServiceTests {

    private val secret = "unit-test-secret-key"
    private val props = JwtProperties(
        secret = secret,
        issuer = "auth-service",
        accessTokenTtlSeconds = 120
    )
    private val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
    private val encoder = NimbusJwtEncoder(ImmutableSecret(secretKey))
    private val decoder = NimbusJwtDecoder.withSecretKey(secretKey)
        .macAlgorithm(MacAlgorithm.HS256)
        .build()
    private val tokenService = TokenService(encoder, props)

    @Test
    fun `issued token contains roles and subject`() {
        val response = tokenService.issueToken("alice", listOf("ROLE_USER"))

        val decoded = decoder.decode(response.accessToken)
        assertEquals("alice", decoded.subject)
        val roles = decoded.claims["roles"] as List<*>
        assertTrue(roles.contains("ROLE_USER"))
        assertEquals(props.issuer, decoded.issuer)
    }
}
