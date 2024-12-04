package com.raynor.demo.core.exception.dto

data class ErrorResponseDto(
    val errors: List<ErrorItemResponseDto>
) {
    data class ErrorItemResponseDto(
        val field: String,
        val message: String
    )
}
