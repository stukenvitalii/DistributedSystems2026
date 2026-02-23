package com.stukenvitalii.lab3.data.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

@Configuration
class JwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    private val authoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
        setAuthoritiesClaimName("roles")
        setAuthorityPrefix("")
    }

    override fun convert(source: Jwt): AbstractAuthenticationToken {
        val authorities = authoritiesConverter.convert(source) ?: emptyList()
        return JwtAuthenticationToken(source, authorities, source.subject)
    }
}

