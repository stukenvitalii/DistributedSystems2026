package com.stukenvitalii.lab3.data.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtAuthenticationConverterConfig {

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        return JwtAuthenticationConverter()  // ← Return the class instance
    }
}