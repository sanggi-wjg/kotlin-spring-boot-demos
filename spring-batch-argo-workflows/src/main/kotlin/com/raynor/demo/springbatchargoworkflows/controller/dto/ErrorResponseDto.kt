package com.raynor.demo.springbatchargoworkflows.controller.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponseDto(
    val status: HttpStatus,
    val error: String,
    val message: String?,
    val detail: String? = null,
    val path: String,
    val requestedAt: Instant = Instant.now()
)
