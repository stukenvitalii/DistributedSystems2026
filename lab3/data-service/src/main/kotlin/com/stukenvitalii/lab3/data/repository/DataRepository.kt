package com.stukenvitalii.lab3.data.repository

import com.stukenvitalii.lab3.data.domain.DataRecord
import org.springframework.stereotype.Repository

@Repository
class DataRepository {

    private val records = listOf(
        DataRecord(1, "Welcome to the secure data service", "PUBLIC"),
        DataRecord(2, "Confidential analytics for Q1", "PROTECTED"),
        DataRecord(3, "System credentials for downstream service", "ADMIN"),
        DataRecord(4, "Service-to-service heartbeat data", "SERVICE")
    )

    fun findBySensitivity(level: String): List<DataRecord> =
        records.filter { it.sensitivity == level }

    fun findPublic(): List<DataRecord> = findBySensitivity("PUBLIC")
}

