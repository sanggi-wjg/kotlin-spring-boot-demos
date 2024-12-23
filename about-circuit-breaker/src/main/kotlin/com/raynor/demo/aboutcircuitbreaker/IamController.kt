package com.raynor.demo.aboutcircuitbreaker

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IamController(
    private val iamService: IamService,
) {
    @GetMapping("/cb")
    fun random(): ResponseEntity<String> {
        return ResponseEntity.ok(iamService.whoAmI())
    }
}