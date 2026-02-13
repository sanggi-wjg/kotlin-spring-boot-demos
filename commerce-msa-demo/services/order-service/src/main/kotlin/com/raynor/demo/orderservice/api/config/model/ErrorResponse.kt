package com.raynor.demo.orderservice.api.config.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.util.*

sealed interface ApiErrorResponse {
    val status: Int
    val error: String
    val message: String
    val path: String?
    val timestamp: Instant
    val trackingId: String
}

data class ErrorResponse(
    override val status: Int,
    override val error: String,
    override val message: String,
    override val path: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    override val timestamp: Instant = Instant.now(),
    override val trackingId: String = UUID.randomUUID().toString(),
    val details: List<String>? = null
) : ApiErrorResponse

data class ValidationErrorResponse(
    override val status: Int,
    override val error: String,
    override val message: String,
    override val path: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    override val timestamp: Instant = Instant.now(),
    override val trackingId: String = UUID.randomUUID().toString(),
    val validationErrors: List<ValidationError>
) : ApiErrorResponse {
    data class ValidationError(
        val field: String,
        val rejectedValue: Any?,
        val message: String
    )
}