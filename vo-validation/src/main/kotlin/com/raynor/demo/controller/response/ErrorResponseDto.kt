package com.raynor.demo.controller.response

data class ErrorResponseDto(
    val errors: List<ErrorItemResponseDto>
) {
    data class ErrorItemResponseDto(
        val field: String,
        val message: String
    )
}
