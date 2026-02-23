package com.stukenvitalii.lab3.data.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityBeansConfig(private val jwtProperties: JwtProperties) {

    private val logger = LoggerFactory.getLogger(SecurityBeansConfig::class.java)

    @Bean
    fun jwtDecoder(): JwtDecoder {
        logger.info("JWT decoder initialized for issuer {}", jwtProperties.issuer)
        return NimbusJwtDecoder.withSecretKey(secretKey())
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    private fun secretKey(): SecretKey {
        val keyBytes = jwtProperties.secret.toByteArray(StandardCharsets.UTF_8)
        return SecretKeySpec(keyBytes, "HmacSHA256")
    }
}
