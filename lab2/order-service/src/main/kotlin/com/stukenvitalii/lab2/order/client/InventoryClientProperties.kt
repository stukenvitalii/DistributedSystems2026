package com.stukenvitalii.lab2.order.client

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("inventory.client")
data class InventoryClientProperties(
    val baseUrl: String,
    val timeout: Duration = Duration.ofSeconds(5),
)

