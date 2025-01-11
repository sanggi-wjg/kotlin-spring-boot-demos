package com.raynor.demo.web.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController {

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    fun me(): ResponseEntity<String> {
        return ResponseEntity.ok("me")
    }
}