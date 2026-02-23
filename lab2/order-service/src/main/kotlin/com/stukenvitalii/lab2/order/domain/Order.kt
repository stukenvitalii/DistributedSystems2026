package com.stukenvitalii.lab2.order.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "external_id", nullable = false, unique = true, length = 40)
    var externalId: String,

    @Column(nullable = false, length = 64)
    var sku: String,

    @Column(nullable = false)
    var quantity: Int,

    @Column(nullable = false, length = 255)
    var itemName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.CONFIRMED,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
)

