package com.stukenvitalii.lab2.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import com.stukenvitalii.lab2.order.client.InventoryClientProperties

@SpringBootApplication
@EnableConfigurationProperties(InventoryClientProperties::class)
class OrderServiceApplication

fun main(vararg args: String) {
    runApplication<OrderServiceApplication>(*args)
}

