package com.stukenvitalii.lab2.inventory.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ItemRepository : JpaRepository<Item, Long> {
    fun findBySku(sku: String): Item?
    fun existsBySku(sku: String): Boolean
}

