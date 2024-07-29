package com.raynor.demo.redis.app

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("")
class SomethingController(
    private val cacheOperationService: CacheOperationService,
) {

    @GetMapping("/list")
    fun list(): ResponseEntity<String> {
        cacheOperationService.listTest()
        return ResponseEntity.ok("hello")
    }

    @GetMapping("/set")
    fun set(): ResponseEntity<String> {
        cacheOperationService.setTest()
        return ResponseEntity.ok("world")
    }

    @GetMapping("/set2")
    fun set2(): ResponseEntity<String> {
        cacheOperationService.setTest2()
        return ResponseEntity.ok("world")
    }
}