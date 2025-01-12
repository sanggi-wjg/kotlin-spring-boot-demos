package com.raynor.demo.web.controller.dto.auth

data class EmailLoginRequestDto(
    val email: String,
    val password: String,
)
