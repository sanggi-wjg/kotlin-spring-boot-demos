package com.raynor.demo.aboutfeign.app

import com.raynor.demo.aboutfeign.app.http.OutSideAPI
import com.raynor.demo.aboutfeign.app.http.OutSideAPIClient
import com.raynor.demo.aboutfeign.app.model.User
import com.raynor.demo.aboutfeign.app.model.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SeekService(
    private val aAPIClient: OutSideAPIClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getUsers(): List<User> {
        return aAPIClient.requestGetUsers()
            .onSuccess { logger.info("Success to getUsers: $it") }
            .onFailure { logger.error("Failed to getUsers", it) }
            .getOrDefault(emptyList())
    }

    fun getUsersUnHandledEnum(): List<User> {
        return aAPIClient.requestGetUsersUnHandledEnum()
            .onSuccess { logger.info("Success to getUsersUnHandledEnum: $it") }
            .onFailure { logger.error("Failed to getUsersUnHandledEnum", it) }
            .getOrThrow() // 실패 했으니 에러 발생 시켜 버림
//            .getOrDefault(emptyList()) // 에러는 에러고 default 값으로 리턴함
    }

    fun getUsersUnHandledEnumButNoRaise(): List<User> {
        return aAPIClient.requestGetUsersUnHandledEnumV2()
            .onSuccess { logger.info("Success to getUsersUnHandledEnumButNoRaise: $it") }
            .onFailure { logger.error("Failed to getUsersUnHandledEnumButNoRaise", it) }
            .getOrThrow()
    }

    fun createUser(): OutSideAPI.UserResponseDto {
        return aAPIClient.requestCreateUser(
            idempotentKey = "12345",
            requestDto = OutSideAPI.UserCreationRequestDto(
                name = "hello world"
            )
        )
            .onSuccess { logger.info("Success to createUser: $it") }
            .onFailure { logger.error("Failed to createUser", it) }
            .getOrThrow()
    }

    fun updateUser(): OutSideAPI.UserResponseDto {
        return aAPIClient.requestUpdateUser(
            userId = 15,
            requestDto = OutSideAPI.UserUpdateRequestDto(
                name = "hello world",
                status = UserStatus.UNKNOWN,
            )
        )
            .onSuccess { logger.info("Success to updateUser: $it") }
            .onFailure { logger.error("Failed to updateUser", it) }
            .getOrThrow()
    }

    fun deleteUser(): Unit {
        return aAPIClient.requestDeleteUser(
            userId = 15
        )
            .onSuccess { logger.info("Success to deleteUser: $it") }
            .onFailure { logger.error("Failed to deleteUser", it) }
            .getOrThrow()
    }
}
