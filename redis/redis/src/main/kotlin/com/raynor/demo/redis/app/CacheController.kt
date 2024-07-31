package com.raynor.demo.redis.app

import com.raynor.demo.redis.app.model.Something
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(this::class.java)

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

            val r1 = redisCacheService.cacheable1(it)
            val r2 = redisCacheService.cacheable2(it)
            val r3 = redisCacheService.cacheable3(it)

            logger.info(r1.toString())
            logger.info(r2.toString())
            logger.info(r3.toString())

            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/ca-2")
    fun cache2(): ResponseEntity<List<Something>> {
        return ResponseEntity.ok(redisCacheService.cacheList())
    }
}