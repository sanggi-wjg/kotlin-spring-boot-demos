package com.raynor.demo.aboutfeign.app.model

import com.raynor.demo.aboutfeign.app.http.OutSideAPI
import org.slf4j.LoggerFactory

data class User(
    val id: Int,
    val name: String,
    val status: UserStatus,
)

fun OutSideAPI.UserResponseDto.toUser() =
    User(
        id = this.id,
        name = this.name,
        status = UserStatus.valueOf(this.status),
    )

fun OutSideAPI.UserResponseDto.toUserV2() =
    User(
        id = this.id,
        name = this.name,
        status = UserStatus.from(this.status),
    )

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    LEFT,
    UNKNOWN;

    companion object {
        fun from(value: String): UserStatus {
            return try {
                UserStatus.valueOf(value)
            } catch (e: IllegalArgumentException) {
                val logger = LoggerFactory.getLogger(this::class.java)
                logger.error("Unknown user status: $value", e)
                UNKNOWN
            }
        }
    }
}
