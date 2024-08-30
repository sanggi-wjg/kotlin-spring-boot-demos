package com.raynor.demo.redis.app

import com.raynor.demo.redis.app.model.Person
import com.raynor.demo.redis.app.model.Something
import com.raynor.demo.redis.app.model.UniqueData
import com.raynor.demo.redis.app.service.IdempotencyService
import com.raynor.demo.redis.app.service.RedisCacheService
import com.raynor.demo.redis.app.service.RedisOperationService
import com.raynor.demo.redis.app.service.RedisRepoService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.random.Random

@RestController
@RequestMapping("")
class CacheController(
    private val redisOperationService: RedisOperationService,
    private val redisCacheService: RedisCacheService,
    private val redisRepoService: RedisRepoService,
    private val idempotencyService: IdempotencyService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/idem-1")
    fun idempotency(
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: String
    ): ResponseEntity<UniqueData> {
        return idempotencyService.validateIdempotency(idempotencyKey) {
            Thread.sleep(10_000)
            UniqueData(1, "hello")
        }
    }

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

    @GetMapping("/set-2")
    fun set2(): ResponseEntity<String> {
        redisOperationService.setTest2()
        return ResponseEntity.ok("world")
    }

    @GetMapping("/ca-1")
    fun cache(): ResponseEntity<Int> {
        return Random.nextInt(2).let {
            /*
            * "GET" "test-cache-key::0"
            * "SET" "test-cache-key::0" "\xac\xed\x00\x05sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02\x00\x01I\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x00\x00" "PX" "60000"
            *
            * "SET" "test-cache-key::0" "\xac\xed\x00\x05sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02\x00\x01I\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x00\x00" "PX" "60000"
            * "DEL" "test-cache-key::0"
            * */
            val r1 = redisCacheService.cacheable(it)
            val r2 = redisCacheService.cachePut(it)
            val r3 = redisCacheService.cacheEvict(it)
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/ca-2")
    fun cache2(): ResponseEntity<Int> {
        val a = 1001
        redisCacheService.cacheable(1)
        redisCacheService.cacheable(2)
        redisCacheService.cacheable(3)

        redisCacheService.cacheEvict(10)
        redisCacheService.cacheEvictWithAllEntries(10)
        return ResponseEntity.ok(a)
    }

    @GetMapping("/ca-3")
    fun cache3(): ResponseEntity<List<Something>> {
        return redisCacheService.cacheList().let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/cr-1")
    fun cr1(): ResponseEntity<Person> {
        return redisRepoService.create().let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/cr-2")
    fun cr2(): ResponseEntity<List<Person>> {
        return redisRepoService.createList().let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/cr-3")
    fun cr3(): ResponseEntity<Person> {
        return redisRepoService.update().let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/cr-4")
    fun cr4(): ResponseEntity<Boolean> {
        return redisRepoService.delete().let {
            ResponseEntity.ok(it)
        }
    }
}