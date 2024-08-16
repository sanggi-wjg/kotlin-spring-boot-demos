package com.raynor.demo.aboutfeign.app.http

import com.raynor.demo.aboutfeign.app.model.UserStatus
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(name = "outSideClient", url = "http://localhost:8080")
interface OutSideAPI {

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/ext/users"],
        consumes = ["application/json"]
    )
    fun getUsers(): List<UserResponseDto>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/ext/users/enum"],
        consumes = ["application/json"]
    )
    fun getUsersUnHandledEnum(): List<UserResponseDto>

    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/ext/users"],
        consumes = ["application/json"]
    )
    fun createUser(
        @RequestHeader idempotentKey: String,
        @RequestBody requestDto: UserCreationRequestDto
    ): UserResponseDto

    @RequestMapping(
        method = [RequestMethod.PATCH],
        value = ["/ext/users/{userId}"],
        consumes = ["application/json"]
    )
    fun updateUser(
        @PathVariable userId: Int,
        @RequestBody requestDto: UserUpdateRequestDto
    ): UserResponseDto

    @RequestMapping(
        method = [RequestMethod.DELETE],
        value = ["/ext/users/{userId}"],
    )
    fun deleteUser(
        @PathVariable userId: Int,
    )

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/ext/delay"],
    )
    fun delayed(): String

    data class UserResponseDto(
        val id: Int,
        val name: String,
        val status: String,
    )

    data class UserCreationRequestDto(
        val name: String
    )

    data class UserUpdateRequestDto(
        val name: String?,
        val status: UserStatus?,
    )
}
