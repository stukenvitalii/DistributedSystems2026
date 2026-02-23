package com.stukenvitalii.lab2.inventory.api

import com.stukenvitalii.lab2.inventory.service.exceptions.InsufficientStockException
import com.stukenvitalii.lab2.inventory.service.exceptions.ItemAlreadyExistsException
import com.stukenvitalii.lab2.inventory.service.exceptions.ItemNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(ItemNotFoundException::class)
    fun handleNotFound(ex: ItemNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.NOT_FOUND, ex.message ?: "Not found", request)

    @ExceptionHandler(ItemAlreadyExistsException::class, InsufficientStockException::class)
    fun handleConflict(ex: RuntimeException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.CONFLICT, ex.message ?: "Conflict", request)

    @ExceptionHandler(BindException::class, MethodArgumentNotValidException::class)
    fun handleValidation(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        buildResponse(
            HttpStatus.BAD_REQUEST,
            when (ex) {
                is BindException -> ex.allErrors.firstOrNull()?.defaultMessage ?: "Validation error"
                is MethodArgumentNotValidException -> ex.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: "Validation error"
                else -> "Validation error"
            },
            request
        )

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "Internal error", request)

    private fun buildResponse(status: HttpStatus, message: String, request: HttpServletRequest) =
        ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = request.requestURI,
            )
        )
}
