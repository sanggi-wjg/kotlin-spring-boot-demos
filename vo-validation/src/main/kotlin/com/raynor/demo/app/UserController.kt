package com.raynor.demo.app

import com.raynor.demo.app.dto.UserCreationRequestDto
import com.raynor.demo.app.dto.toModel
import com.raynor.demo.app.dto.toUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @GetMapping("/{userId}")
    fun getUserById(
        @PathVariable userId: Int
    ): ResponseEntity<UserEntity> {
        return userService.getUser(
            userId.toUserId()
        ).let {
            ResponseEntity.ok(it)
        }
    }
}
