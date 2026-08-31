package com.raynor.demo.boiler.controller.support

import java.time.Instant

data class ErrorResponseDto(
    val status: Int,
    val statusText: String,
    val requestedAt: Instant = Instant.now(),
    val message: String? = null,
    val details: List<ErrorDetail>? = null,
) {
    sealed class ErrorDetail

    data class FieldError(
        val field: String,
        val message: String,
    ) : ErrorDetail()
}
