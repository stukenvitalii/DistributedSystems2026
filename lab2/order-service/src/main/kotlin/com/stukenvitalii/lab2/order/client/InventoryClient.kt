package com.stukenvitalii.lab2.order.client

import com.stukenvitalii.lab2.order.client.dto.InventoryItemResponse
import com.stukenvitalii.lab2.order.client.dto.ReserveStockPayload
import com.stukenvitalii.lab2.order.service.exceptions.InventoryServiceException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class InventoryClient(
    builder: WebClient.Builder,
    properties: InventoryClientProperties,
) {
    private val client = builder
        .baseUrl(properties.baseUrl)
        .build()

    fun reserveStock(sku: String, quantity: Int): InventoryItemResponse {
        return client.post()
            .uri("/api/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ReserveStockPayload(sku, quantity))
            .retrieve()
            .onStatus({ !it.is2xxSuccessful }) { response ->
                val status = response.statusCode()
                response.bodyToMono<String>()
                    .defaultIfEmpty(status.asReason())
                    .flatMap { body -> Mono.error(InventoryServiceException(status, body)) }
            }
            .bodyToMono<InventoryItemResponse>()
            .block() ?: throw InventoryServiceException(HttpStatusCode.valueOf(502), "Inventory response is empty")
    }

    private fun HttpStatusCode.asReason(): String =
        (this as? HttpStatus)?.reasonPhrase ?: "HTTP ${value()}"
}
