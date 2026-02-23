package com.stukenvitalii.lab3.auth.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class SecurityBeansConfig(private val jwtProperties: JwtProperties) {

    private val logger = LoggerFactory.getLogger(SecurityBeansConfig::class.java)

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtEncoder(): JwtEncoder = NimbusJwtEncoder(ImmutableSecret(secretKey()))

    @Bean
    fun jwtDecoder(): NimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey())
        .macAlgorithm(MacAlgorithm.HS256)
        .build()

    private fun secretKey(): SecretKey {
        val keyBytes = jwtProperties.secret.toByteArray(StandardCharsets.UTF_8)
        return SecretKeySpec(keyBytes, "HmacSHA256")
    }

    @PostConstruct
    fun logIssuer() {
        logger.info(
            "JWT issuer configured as {} with TTL {}s",
            jwtProperties.issuer,
            jwtProperties.accessTokenTtlSeconds
        )
    }
}
