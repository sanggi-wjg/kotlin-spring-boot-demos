package com.raynor.demo.aboutfeign.outside

data class OutSideUserResponseDto(
    val id: Long,
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
