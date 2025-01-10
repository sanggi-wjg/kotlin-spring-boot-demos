package com.raynor.demo.web.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PingPingEController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/ping")
    fun ping(
        authentication: Authentication?,
    ): ResponseEntity<String> {
        if (authentication != null) {
            return ResponseEntity.ok(authentication.principal.toString())
        }
        return ResponseEntity.ok("pong")
    }

    @GetMapping("/pong")
    fun pong(): ResponseEntity<String> {
        return ResponseEntity.ok("ping")
    }
}