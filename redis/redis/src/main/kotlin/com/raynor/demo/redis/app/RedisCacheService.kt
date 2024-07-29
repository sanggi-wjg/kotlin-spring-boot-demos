package com.raynor.demo.redis.app

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.stereotype.Service

@Service
class RedisCacheService(
    private val cacheManager: RedisCacheManager
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Cacheable("test-key")
    fun get(randInt: Int): Int {
        /*
        * "GET" "test-key::2"
        * If missed, then below.
        * "SET" "test-key::2" "\xac\xed\x00\x05sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02\x00\x01I\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x00\x14" "PX" "10000"
        * */
        return randInt * 10
    }
}