package com.raynor.demo.aboutfeign.outside.dto

data class OutSideUserResponseDto(
    val id: Int,
    val name: String,
    val status: OutSideUserStatus,
)

enum class OutSideUserStatus {
    ACTIVE,
    INACTIVE,
    LEFT,
    KICKED,
    MIA,
}
