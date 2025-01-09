package com.raynor.demo.web.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class UserController {

    @GetMapping("/users/me")
    fun me(): ResponseEntity<String> {
        return ResponseEntity.ok("me")
    }
}