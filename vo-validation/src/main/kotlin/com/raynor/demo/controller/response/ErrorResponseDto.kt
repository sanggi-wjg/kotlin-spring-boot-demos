package com.raynor.demo.controller.response

data class ErrorResponseDto(
    val code: Int,
    val message: String,
    val errors: List<ErrorItemResponseDto>
) {
    data class ErrorItemResponseDto(
        val field: String,
        val message: String,
    )
}
