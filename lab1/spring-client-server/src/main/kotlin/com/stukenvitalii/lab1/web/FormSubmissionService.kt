package com.stukenvitalii.lab1.web

import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

@Service
class FormSubmissionService {
    private val sequence = AtomicInteger()
    private val storage = CopyOnWriteArrayList<SubmissionRecord>()

    fun registerSubmission(request: SubmissionRequest): SubmissionRecord {
        val record = SubmissionRecord(
            id = sequence.incrementAndGet(),
            senderName = request.senderName.trim(),
            payload = request.payload.trim(),
            receivedAt = Instant.now()
        )
        storage += record
        return record
    }

    fun listSubmissions(): List<SubmissionRecord> = storage
        .sortedByDescending(SubmissionRecord::receivedAt)

    fun clear() {
        storage.clear()
        sequence.set(0)
    }
}
