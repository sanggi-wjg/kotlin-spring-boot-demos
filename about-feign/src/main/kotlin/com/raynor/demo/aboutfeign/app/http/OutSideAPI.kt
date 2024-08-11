package com.raynor.demo.aboutfeign.app.http

import com.raynor.demo.aboutfeign.app.model.UserStatus
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(name = "outSideClient", url = "http://localhost:8080/ext")
interface OutSideAPI {

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/users"],
        consumes = ["application/json"]
    )
    fun getUsers(): List<UserResponseDto>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/users/enum"],
        consumes = ["application/json"]
    )
    fun getUsersUnHandledEnum(): List<UserResponseDto>

    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/users"],
        consumes = ["application/json"]
    )
    fun createUser(
        @RequestHeader idempotentKey: String,
        @RequestBody requestDto: UserCreationRequestDto
    ): UserResponseDto

    @RequestMapping(
        method = [RequestMethod.PATCH],
        value = ["/users/{userId}"],
        consumes = ["application/json"]
    )
    fun updateUser(
        @PathVariable userId: Int,
        @RequestBody requestDto: UserUpdateRequestDto
    ): UserResponseDto

    @RequestMapping(
        method = [RequestMethod.DELETE],
        value = ["/users/{userId}"],
    )
    fun deleteUser(
        @PathVariable userId: Int,
    )

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
