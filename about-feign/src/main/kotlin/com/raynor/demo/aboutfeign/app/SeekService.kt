package com.raynor.demo.aboutfeign.app

import com.raynor.demo.aboutfeign.app.http.DynamicUrlAPI
import com.raynor.demo.aboutfeign.app.http.OutSideAPI
import com.raynor.demo.aboutfeign.app.http.OutSideAPIClient
import com.raynor.demo.aboutfeign.app.model.User
import com.raynor.demo.aboutfeign.app.model.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI

@Service
class SeekService(
    private val outSideAPIClient: OutSideAPIClient,
    private val dynamicUrlAPI: DynamicUrlAPI,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getUsers(): List<User> {
        return outSideAPIClient.requestGetUsers()
            .onSuccess { logger.info("Success to getUsers: $it") }
            .onFailure { logger.error("Failed to getUsers", it) }
            .getOrDefault(emptyList())
    }

    fun getUsersUnHandledEnum(): List<User> {
        return outSideAPIClient.requestGetUsersUnHandledEnum()
            .onSuccess { logger.info("Success to getUsersUnHandledEnum: $it") }
            .onFailure { logger.error("Failed to getUsersUnHandledEnum", it) }
            .getOrThrow() // 실패 했으니 에러 발생 시켜 버림
//            .getOrDefault(emptyList()) // 에러는 에러고 default 값으로 리턴함
    }

    fun getUsersUnHandledEnumButNoRaise(): List<User> {
        return outSideAPIClient.requestGetUsersUnHandledEnumV2()
            .onSuccess { logger.info("Success to getUsersUnHandledEnumButNoRaise: $it") }
            .onFailure { logger.error("Failed to getUsersUnHandledEnumButNoRaise", it) }
            .getOrThrow()
    }

    fun createUser(): OutSideAPI.UserResponseDto {
        return outSideAPIClient.requestCreateUser(
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
        return outSideAPIClient.requestUpdateUser(
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
        return outSideAPIClient.requestDeleteUser(
            userId = 15
        )
            .onSuccess { logger.info("Success to deleteUser: $it") }
            .onFailure { logger.error("Failed to deleteUser", it) }
            .getOrThrow()
    }

    fun requestWithDynamicURL() {
        runCatching {
            dynamicUrlAPI.getSomething(
                uri = URI.create("https://jsonplaceholder.typicode.com/posts/2")
            )
        }.onSuccess { logger.info("Success to requestWithDynamicURL: $it") }
            .onFailure { logger.error("Failed to requestWithDynamicURL", it) }
    }

    fun requestDelayed() {
        runCatching {
            outSideAPIClient.requestDelayed()
        }.onSuccess { logger.info("Success to request delayed: $it") }
            .onFailure { logger.error("Failed to request delayed", it) }
    }
}
