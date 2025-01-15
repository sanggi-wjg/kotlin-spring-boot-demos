package com.raynor.demo.controller

import com.raynor.demo.controller.mapper.toModel
import com.raynor.demo.controller.mapper.toUserId
import com.raynor.demo.controller.mapper.toUserResponseDto
import com.raynor.demo.controller.request.UserCreationRequestDto
import com.raynor.demo.controller.response.UserResponseDto
import com.raynor.demo.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {

    @PostMapping("")
    fun createUser(@RequestBody requestDto: UserCreationRequestDto): ResponseEntity<UserResponseDto> {
        return userService.createUser(
            requestDto.toModel()
        ).let {
            ResponseEntity.ok(it.toUserResponseDto())
        }
    }

    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: Int): ResponseEntity<UserResponseDto> {
        return userService.getUser(
            userId.toUserId()
        ).let {
            ResponseEntity.ok(it.toUserResponseDto())
        }
    }
}
