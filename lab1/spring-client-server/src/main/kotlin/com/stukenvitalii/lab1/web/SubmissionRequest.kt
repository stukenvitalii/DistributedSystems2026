package com.stukenvitalii.lab1.web

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SubmissionRequest(
    @field:NotBlank(message = "Имя обязательно")
    @field:Size(max = 40, message = "Имя должно быть короче 40 символов")
    val senderName: String = "",

    @field:NotBlank(message = "Сообщение не может быть пустым")
    @field:Size(min = 5, max = 240, message = "Сообщение должно быть от 5 до 240 символов")
    val payload: String = ""
)
