package com.raynor.demo.redis.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.redis.app.model.Something
import com.raynor.demo.redis.app.model.SomethingStatus
import com.raynor.demo.redis.app.model.UniqueData
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.SetOperations
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class CacheOperationService(
    private val objectMapper: ObjectMapper,
    private val listOps: ListOperations<String, String>,
    private val setOps: SetOperations<String, String>,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val listKey by lazy { "key-list" }
    private val listKey2 by lazy { "key-list-2" }

    private val setKey by lazy { "key-set" }
    private val setKey2 by lazy { "key-set-2" }

    /*
    * https://docs.spring.io/spring-data/redis/reference/redis/template.html
    * */
    fun listTest() {
        // "LPUSH" "key-1" "value-1"
        listOps.leftPush(listKey, "value-1")

        // "LRANGE" "key-1" "0" "-1"
        val res = listOps.range(listKey, 0, -1) ?: listOf()
        logger.info(res.toString())

        // "RPUSH" "key-list-2" "{\"id\":111,\"name\":\"hello world\",\"amount\":0,\"status\":\"INACTIVE\",\"createdAt\":\"2024-07-28T15:01:18.955175Z\"}"
        val data = objectMapper.writeValueAsString(
            Something(111, "hello world", BigDecimal.ZERO, SomethingStatus.INACTIVE, Instant.now())
        )
        listOps.rightPush(listKey2, data)

        // "LRANGE" "key-list-2" "0" "-1"
        // [ {"id":111,"name":"hello world","amount":0,"status":"INACTIVE","createdAt":"2024-07-28T15:01:18.955175Z"},
        //   {"id":111,"name":"hello world","amount":0,"status":"INACTIVE","createdAt":"2024-07-28T15:01:42.354801Z"}]
        val res2 = listOps.range(listKey2, 0, -1) ?: listOf()
        logger.info(res2.toString())
    }

    fun setTest() {
        // "SADD" "key-set" "{\"id\":1,\"name\":\"hello\",\"amount\":1,\"status\":\"ACTIVE\",\"createdAt\":\"2024-07-28T15:02:20.282320Z\"}"
        val data = objectMapper.writeValueAsString(
            Something(1, "hello", BigDecimal.ONE, SomethingStatus.ACTIVE, Instant.now())
        )
        setOps.add(setKey, data)

        // [ {"id":1,"name":"hello","amount":1,"status":"ACTIVE","createdAt":"2024-07-28T15:02:20.282320Z"},
        //   {"id":1,"name":"hello","amount":1,"status":"ACTIVE","createdAt":"2024-07-28T15:02:14.528917Z"}]
        // Something(id=1, name=hello, amount=1, status=ACTIVE, createdAt=2024-07-28T15:05:13.779234Z)
        // Something(id=1, name=hello, amount=1, status=ACTIVE, createdAt=2024-07-28T15:02:20.282320Z)
        // Something(id=1, name=hello, amount=1, status=ACTIVE, createdAt=2024-07-28T15:02:14.528917Z)
        val res = setOps.members(setKey)
        res?.forEach {
            logger.info(objectMapper.readValue(it, Something::class.java).toString())
        }
    }

    fun setTest2() {
        // "SADD" "key-set-2" "{\"uniqueId\":1,\"name\":\"hello\"}"
        val data = objectMapper.writeValueAsString(
            UniqueData(1, "hello")
        )
        setOps.add(setKey2, data)

        // "SMEMBERS" "key-set-2"
        // UniqueData(uniqueId=1, name=hello)
        val res = setOps.members(setKey2)
        res?.forEach {
            logger.info(objectMapper.readValue(it, UniqueData::class.java).toString())
        }
    }
}
