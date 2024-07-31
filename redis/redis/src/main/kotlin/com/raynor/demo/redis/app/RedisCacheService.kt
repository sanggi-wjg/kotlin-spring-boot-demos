package com.raynor.demo.redis.app

import com.raynor.demo.redis.app.model.Something
import com.raynor.demo.redis.app.model.SomethingStatus
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RedisCacheService(
    private val cacheManager: RedisCacheManager
) {
    /*
    * Cacheable 어노테이션 api docs
    * https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/cache/annotation/Cacheable.html
    * */

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Cacheable(key = "{#randInt}", value = ["test-cache-key"])
    fun cacheable1(randInt: Int): Int {
        return randInt * 10
    }

    @CachePut(key = "{#randInt}", value = ["test-cache-key"])
    fun cacheable2(randInt: Int): Int {
        return randInt * 100
    }

    @CacheEvict(key = "{#randInt}", value = ["test-cache-key"])
    fun cacheable3(randInt: Int): Int {
        return randInt * 1000
    }

    @Cacheable("test-cache-list-key")
    fun cacheList(): List<Something> {
        /*
        * "GET" "test-cache-list-key::SimpleKey []"
        *
        * "SET" "test-cache-list-key::SimpleKey []" "\xac\xed\x00\x05sr\x00\x1ajava.util.Arrays$ArrayList\xd9\xa4<\xbe\xcd\x88\x06\xd2\x02\x00\x01[\x00\x01at\x00\x13[Ljava/lang/Object;xpur\x00,
        * [Lcom.raynor.demo.redis.app.model.Something;\xe4`z[e*0\xc4\x02\x00\x00xp\x00\x00\x00\x04sr\x00)com.raynor.demo.redis.app.model.Something\xb9T\x0f1\x0b\xe1\x0b\x9b\x02\x00\x05I\x00\x02idL
        * \x00\x06amountt\x00\x16Ljava/math/BigDecimal;L\x00\tcreatedAtt\x00\x13Ljava/time/Instant;L\x00\x04namet\x00\x12Ljava/lang/String;L\x00\x06statust\x001Lcom/raynor/demo/redis/app/model/SomethingStatus;
        * xp\x00\x00\x00\x01sr\x00\x14java.math.BigDecimalT\xc7\x15W\xf9\x81(O\x03\x00\x02I\x00\x05scaleL\x00\x06intValt\x00\x16Ljava/math/BigInteger;xr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00
        * xp\x00\x00\x00\x00sr\x00\x14java.math.BigInteger\x8c\xfc\x9f\x1f\xa9;\xfb\x1d\x03\x00\x06I\x00\bbitCountI\x00\tbitLengthI\x00\x13firstNonzeroByteNumI\x00\x0clowestSetBitI\x00\x06signum[\x00\tmagnitudet\x00\x02[B
        * xq\x00~\x00\r\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xfe\xff\xff\xff\xfe\x00\x00\x00\x01ur\x00\x02[B\xac\xf3\x17\xf8\x06\bT\xe0\x02\x00\x00xp\x00\x00\x00\x01\x03xxsr\x00\rjava.time.Ser\x95]\x84\xba\x1b\
        * "H\xb2\x0c\x00\x00xpw\r\x02\x00\x00\x00\x00f\xa8\xb1\xe4\x1a\x04\x89\xa8xt\x00\x06Spring~r\x00/com.raynor.demo.redis.app.model.SomethingStatus\x00\x00\x00\x00\x00\x00\x00\x00\x12\x00\x00xr\x00\x0ejava.lang.Enum
        * \x00\x00\x00\x00\x00\x00\x00\x00\x12\x00\x00xpt\x00\bINACTIVEsq\x00~\x00\x05\x00\x00\x00\x02sq\x00~\x00\x0b\x00\x00\x00\x00sq\x00~\x00\x0f\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xfe\xff\xff\xff\xfe\x00\x00\x00\x01
        * uq\x00~\x00\x12\x00\x00\x00\x01\x06xxsq\x00~\x00\x14w\r\x02\x00\x00\x00\x00f\xa8\xb1\xe4\x1a\x04\xb4\xa0xt\x00\x06Summer~q\x00~\x00\x17t\x00\x06ACTIVEsq\x00~\x00\x05\x00\x00\x00\x03sq\x00~\x00\x0b\x00\x00\x00\x00sq\x00~
        * \x00\x0f\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xfe\xff\xff\xff\xfe\x00\x00\x00\x01uq\x00~\x00\x12\x00\x00\x00\x01\nxxsq\x00~\x00\x14w\r\x02\x00\x00\x00\x00f\xa8\xb1\xe4\x1a\x04\xb8\x88xt\x00\x06Autumnq\x00~\x00!sq\
        * x00~\x00\x05\x00\x00\x00\x04sq\x00~\x00\x0b\x00\x00\x00\x00sq\x00~\x00\x0f\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xfe\xff\xff\xff\xfe\x00\x00\x00\x01uq\x00~\x00\x12\x00\x00\x00\x01\x0cxxsq\x00~\x00\x14w\r\x02\x00\x00
        * \x00\x00f\xa8\xb1\xe4\x1a\x04\xc0Xxt\x00\x06Winterq\x00~\x00\x19" "PX" "60000"
        * */
        return listOf(
            Something(1, "Spring", 3.toBigDecimal(), SomethingStatus.INACTIVE, Instant.now()),
            Something(2, "Summer", 6.toBigDecimal(), SomethingStatus.ACTIVE, Instant.now()),
            Something(3, "Autumn", 10.toBigDecimal(), SomethingStatus.ACTIVE, Instant.now()),
            Something(4, "Winter", 12.toBigDecimal(), SomethingStatus.INACTIVE, Instant.now()),
        )
    }
}