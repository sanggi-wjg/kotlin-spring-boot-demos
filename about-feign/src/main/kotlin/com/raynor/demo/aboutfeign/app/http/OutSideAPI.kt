package com.raynor.demo.aboutfeign.app.http

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

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

    data class UserResponseDto(
        val id: Int,
        val name: String,
        val status: String,
    )
}
