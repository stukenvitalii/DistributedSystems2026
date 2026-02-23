package com.stukenvitalii.lab3.auth.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<Map<String, Any>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            mapOf(
                "error" to "invalid_credentials",
                "message" to (ex.message ?: "Bad credentials")
            )
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "error" to "validation_error",
                "message" to ex.bindingResult.fieldErrors.joinToString { formatFieldError(it) }
            )
        )

    private fun formatFieldError(fieldError: FieldError): String =
        "${fieldError.field} ${fieldError.defaultMessage}"
}

