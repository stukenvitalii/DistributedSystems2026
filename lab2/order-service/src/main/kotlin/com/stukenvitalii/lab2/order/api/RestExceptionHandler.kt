package com.stukenvitalii.lab2.order.api

import com.stukenvitalii.lab2.order.service.exceptions.InventoryServiceException
import com.stukenvitalii.lab2.order.service.exceptions.OrderAlreadyExistsException
import com.stukenvitalii.lab2.order.service.exceptions.OrderNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleNotFound(ex: OrderNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.NOT_FOUND, ex.message ?: "Not found", request)

    @ExceptionHandler(OrderAlreadyExistsException::class)
    fun handleConflict(ex: OrderAlreadyExistsException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.CONFLICT, ex.message ?: "Conflict", request)

    @ExceptionHandler(InventoryServiceException::class)
    fun handleInventory(ex: InventoryServiceException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        buildResponse(ex.status, ex.message, request)

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

    private fun buildResponse(status: HttpStatusCode, message: String, request: HttpServletRequest) =
        ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = (status as? HttpStatus)?.reasonPhrase ?: "HTTP ${status.value()}",
                message = message,
                path = request.requestURI,
            )
        )
}
