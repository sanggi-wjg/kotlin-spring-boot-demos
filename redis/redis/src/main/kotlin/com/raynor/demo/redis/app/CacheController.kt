package com.raynor.demo.redis.app

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random

@RestController
@RequestMapping("")
class CacheController(
    private val redisOperationService: RedisOperationService,
    private val redisCacheService: RedisCacheService,
) {

    @GetMapping("/list")
    fun list(): ResponseEntity<String> {
        redisOperationService.listTest()
        return ResponseEntity.ok("hello")
    }

    @GetMapping("/set")
    fun set(): ResponseEntity<String> {
        redisOperationService.setTest()
        return ResponseEntity.ok("world")
    }

    @GetMapping("/set2")
    fun set2(): ResponseEntity<String> {
        redisOperationService.setTest2()
        return ResponseEntity.ok("world")
    }

    @GetMapping("/cache")
    fun cache(): ResponseEntity<String> {
        redisCacheService.get(Random.nextInt(10))
        return ResponseEntity.ok("world")
    }
}