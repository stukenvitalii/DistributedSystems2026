package com.stukenvitalii.lab3.data.web

import com.stukenvitalii.lab3.data.api.SecureMessageRequest
import com.stukenvitalii.lab3.data.api.SecureMessageResponse
import com.stukenvitalii.lab3.data.domain.DataRecord
import com.stukenvitalii.lab3.data.service.DataService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/data")
class DataController(private val dataService: DataService) {

    @GetMapping("/public")
    fun publicData(): ResponseEntity<List<DataRecord>> =
        ResponseEntity.ok(dataService.fetchPublic())

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/protected")
    fun protectedData(): ResponseEntity<List<DataRecord>> =
        ResponseEntity.ok(dataService.fetchProtected())

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    fun adminData(): ResponseEntity<List<DataRecord>> =
        ResponseEntity.ok(dataService.fetchAdmin())

    @PreAuthorize("hasRole('SERVICE')")
    @PostMapping("/secure-message")
    fun secureMessage(
        @Valid @RequestBody body: SecureMessageRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SecureMessageResponse> = ResponseEntity.ok(
        SecureMessageResponse(
            receivedAt = Instant.now(),
            echoedPayload = body.payload,
            processedBy = jwt.subject
        )
    )
}

