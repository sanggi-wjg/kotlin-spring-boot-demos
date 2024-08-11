package com.raynor.demo.aboutfeign.app.http

import com.raynor.demo.aboutfeign.app.model.User
import com.raynor.demo.aboutfeign.app.model.toUser
import com.raynor.demo.aboutfeign.app.model.toUserV2
import org.springframework.stereotype.Component

@Component
class OutSideAPIClient(
    private val outSideAPI: OutSideAPI,
) {
    fun requestGetUsers(): Result<List<User>> {
        return runCatching {
            outSideAPI.getUsers().map { it.toUser() }
        }
    }

    fun requestGetUsersUnHandledEnum(): Result<List<User>> {
        return runCatching {
            outSideAPI.getUsersUnHandledEnum().map { it.toUser() }
        }
    }

    fun requestGetUsersUnHandledEnumV2(): Result<List<User>> {
        return runCatching {
            outSideAPI.getUsersUnHandledEnum().map { it.toUserV2() }
        }
    }

    fun requestCreateUser(
        idempotentKey: String,
        requestDto: OutSideAPI.UserCreationRequestDto
    ): Result<OutSideAPI.UserResponseDto> {
        return runCatching {
            outSideAPI.createUser(
                idempotentKey = idempotentKey,
                requestDto = requestDto
            )
        }
    }

    fun requestUpdateUser(
        userId: Int,
        requestDto: OutSideAPI.UserUpdateRequestDto
    ): Result<OutSideAPI.UserResponseDto> {
        return runCatching {
            outSideAPI.updateUser(
                userId = userId,
                requestDto = requestDto
            )
        }
    }

    fun requestDeleteUser(userId: Int): Result<Unit> {
        return runCatching {
            outSideAPI.deleteUser(
                userId = userId
            )
        }
    }
}
