package com.stukenvitalii.lab2.order.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

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
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
)

