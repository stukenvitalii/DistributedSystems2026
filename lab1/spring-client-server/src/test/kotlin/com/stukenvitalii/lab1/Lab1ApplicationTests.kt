package com.stukenvitalii.lab1

import com.stukenvitalii.lab1.web.FormSubmissionService
import com.stukenvitalii.lab1.web.SubmissionRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired

@SpringBootTest
class Lab1ApplicationTests {

    @Autowired
    private lateinit var submissionService: FormSubmissionService

    @AfterEach
    fun tearDown() {
        submissionService.clear()
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun `submissions are stored and returned in reverse chronological order`() {
        val first = submissionService.registerSubmission(SubmissionRequest("Alice", "Первое сообщение"))
        Thread.sleep(5)
        val second = submissionService.registerSubmission(SubmissionRequest("Bob", "Второе сообщение"))
        val records = submissionService.listSubmissions()
        assertTrue(records.containsAll(listOf(first, second)))
        assertEquals(second.id, records.first().id)
    }

}
