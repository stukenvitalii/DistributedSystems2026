package com.stukenvitalii.lab3.data.web

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.Instant

@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val start = Instant.now()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val elapsed = Duration.between(start, Instant.now()).toMillis()
            val principal = request.userPrincipal?.name ?: "anonymous"
            logger.info(
                "{} {} -> {} (user={}, {} ms)",
                request.method,
                request.requestURI,
                response.status,
                principal,
                elapsed
            )
        }
    }
}

