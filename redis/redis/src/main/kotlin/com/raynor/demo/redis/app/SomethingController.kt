package com.raynor.demo.redis.app

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("")
class SomethingController(
    private val cacheService: CacheService,
) {

    @GetMapping("/list")
    fun list(): ResponseEntity<String> {
        cacheService.listTest()
        return ResponseEntity.ok("hello")
    }

    @GetMapping("/set")
    fun set(): ResponseEntity<String> {
        cacheService.setTest()
        return ResponseEntity.ok("world")
    }

    @GetMapping("/set2")
    fun set2(): ResponseEntity<String> {
        cacheService.setTest2()
        return ResponseEntity.ok("world")
    }
}