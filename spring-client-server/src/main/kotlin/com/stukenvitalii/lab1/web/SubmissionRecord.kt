package com.stukenvitalii.lab1.web

import java.time.Instant

data class SubmissionRecord(
    val id: Int,
    val senderName: String,
    val payload: String,
    val receivedAt: Instant
)
