package com.raynor.demo.aboutfeign.outside

import com.raynor.demo.aboutfeign.outside.dto.OutSideUserCreationRequestDto
import com.raynor.demo.aboutfeign.outside.dto.OutSideUserResponseDto
import com.raynor.demo.aboutfeign.outside.dto.OutSideUserStatus
import com.raynor.demo.aboutfeign.outside.dto.OutSideUserUpdateRequestDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/ext")
class OutSideController {

    @GetMapping("/users")
    fun users(): ResponseEntity<List<OutSideUserResponseDto>> {
        return ResponseEntity.ok(
            listOf(
                OutSideUserResponseDto(1, "user-1", OutSideUserStatus.ACTIVE),
                OutSideUserResponseDto(999, "user-2", OutSideUserStatus.INACTIVE),
                OutSideUserResponseDto(1631, "user-3", OutSideUserStatus.LEFT),
                OutSideUserResponseDto(10448, "user-4", OutSideUserStatus.ACTIVE),
            )
        )
    }

    @GetMapping("/users/enum")
    fun usersUnHandledEnum(): ResponseEntity<List<OutSideUserResponseDto>> {
        return ResponseEntity.ok(
            listOf(
                OutSideUserResponseDto(1, "user-1", OutSideUserStatus.ACTIVE),
                OutSideUserResponseDto(999, "user-2", OutSideUserStatus.INACTIVE),
                OutSideUserResponseDto(1631, "user-3", OutSideUserStatus.LEFT),
                OutSideUserResponseDto(10448, "user-4", OutSideUserStatus.ACTIVE),
                OutSideUserResponseDto(12345, "user-4", OutSideUserStatus.KICKED),
                OutSideUserResponseDto(56789, "user-4", OutSideUserStatus.MIA),
            )
        )
    }

    @PostMapping("/users")
    fun createUser(
        @RequestHeader(required = true) idempotentKey: String,
        @RequestBody requestDto: OutSideUserCreationRequestDto,
    ): ResponseEntity<OutSideUserResponseDto> {
        return ResponseEntity
            .created(URI.create("http://localhost:8080/ext/users/$idempotentKey"))
            .body(
                OutSideUserResponseDto(idempotentKey.toInt(), "user-$idempotentKey", OutSideUserStatus.ACTIVE)
            )
    }

    @PatchMapping("/users/{userId}")
    fun updateUser(
        @PathVariable userId: Int,
        @RequestBody requestDto: OutSideUserUpdateRequestDto
    ): ResponseEntity<OutSideUserResponseDto> {
        if (userId == 10) {
            throw RuntimeException("wtf, do not request with 10")
        }
        return ResponseEntity.ok(
            OutSideUserResponseDto(userId, requestDto.name ?: "changed", requestDto.status ?: OutSideUserStatus.KICKED)
        )
    }

    @DeleteMapping("/users/{userId}")
    fun deleteUser(
        @PathVariable userId: Int
    ): ResponseEntity<Unit> {
        if (userId == 10) {
            throw RuntimeException("wtf, do not request with 10")
        }
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/delay")
    fun delayed(): ResponseEntity<String> {
        Thread.sleep(90000)
        return ResponseEntity.accepted().body("hello world - DELAYED")
    }
}
