package com.raynor.demo.web.controller.dto.auth

data class EmailSignupRequestDto(
    val email: String,
    val name: String,
    val rawPassword: String,
)
