package com.raynor.demo.batchbulkexcel.api.controller.dto

import org.springframework.http.HttpStatus
import java.time.Instant

data class ErrorResponseDto(
    val status: HttpStatus,
    val error: String,
    val message: String?,
    val path: String,
    val requestedAt: Instant = Instant.now(),
)
