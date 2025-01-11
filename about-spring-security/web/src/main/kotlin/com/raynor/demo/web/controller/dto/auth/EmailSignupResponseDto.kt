package com.raynor.demo.web.controller.dto.auth

import com.raynor.demo.mysql.entity.UserEntity
import java.time.Instant

data class EmailSignupResponseDto(
    val name: String,
    val email: String,
    val joinedAt: Instant,
)

fun UserEntity.toEmailSignupResponseDto(): EmailSignupResponseDto {
    return EmailSignupResponseDto(
        name = name,
        email = email,
        joinedAt = createdAt,
    )
}
