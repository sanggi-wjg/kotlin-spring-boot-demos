package com.raynor.demo.web.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController {

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('GENERAL')")
    fun me(
        authentication: Authentication?,
    ): ResponseEntity<String> {
        return ResponseEntity.ok("me")
    }
}