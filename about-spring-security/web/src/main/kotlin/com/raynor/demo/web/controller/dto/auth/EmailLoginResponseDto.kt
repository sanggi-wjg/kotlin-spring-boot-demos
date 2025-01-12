package com.raynor.demo.web.controller.dto.auth

import com.raynor.demo.mysql.entity.DeviceEntity

data class EmailLoginResponseDto(
    val grant: String,
    val accessToken: String,
    val refreshToken: String,
)

fun DeviceEntity.toEmailLoginResponseDto(): EmailLoginResponseDto {
    return EmailLoginResponseDto(
        grant = "Bearer",
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}