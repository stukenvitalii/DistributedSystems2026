package com.stukenvitalii.lab2.order.domain

import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun existsByExternalId(externalId: String): Boolean
}

