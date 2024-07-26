package com.raynor.demo.app

import com.raynor.demo.app.dto.UserCreationRequestDto
import com.raynor.demo.app.dto.toModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {

    @PostMapping("")
    fun createUser(
        @RequestBody requestDto: UserCreationRequestDto,
    ): ResponseEntity<UserEntity> {
        return userService.createUser(
            requestDto.toModel()
        ).let {
            ResponseEntity.ok(it)
        }
    }
}
