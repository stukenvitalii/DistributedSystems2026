package com.stukenvitalii.lab3.auth.service

import com.stukenvitalii.lab3.auth.api.TokenResponse
import com.stukenvitalii.lab3.auth.config.JwtProperties
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TokenService(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: JwtProperties
) {

    fun issueToken(username: String, roles: Collection<String>): TokenResponse {
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plusSeconds(jwtProperties.accessTokenTtlSeconds)
        val claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer)
            .subject(username)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim("roles", roles)
            .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        return TokenResponse(
            accessToken = token,
            issuedAt = issuedAt,
            expiresAt = expiresAt
        )
    }
}
