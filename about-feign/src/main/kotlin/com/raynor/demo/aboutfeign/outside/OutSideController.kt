package com.raynor.demo.aboutfeign.outside

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}