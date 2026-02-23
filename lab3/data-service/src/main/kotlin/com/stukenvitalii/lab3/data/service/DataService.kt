package com.stukenvitalii.lab3.data.service

import com.stukenvitalii.lab3.data.domain.DataRecord
import com.stukenvitalii.lab3.data.repository.DataRepository
import org.springframework.stereotype.Service

@Service
class DataService(private val repository: DataRepository) {

    fun fetchPublic(): List<DataRecord> = repository.findPublic()

    fun fetchProtected(): List<DataRecord> = repository.findBySensitivity("PROTECTED")

    fun fetchAdmin(): List<DataRecord> = repository.findBySensitivity("ADMIN")

    fun fetchServiceData(): List<DataRecord> = repository.findBySensitivity("SERVICE")
}

