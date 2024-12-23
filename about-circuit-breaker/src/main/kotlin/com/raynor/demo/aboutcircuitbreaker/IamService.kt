package com.raynor.demo.aboutcircuitbreaker

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.Exception
import kotlin.random.Random

@Service
class IamService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @CircuitBreaker(name = "transaction", fallbackMethod = "fallback")
    fun whoAmI(): String {
        if (Random.nextBoolean()) {
            logger.info("throw exception")
            throw RuntimeException("something wrong")
        }

        Random.nextLong(0, 10_000).also {
            logger.info("sleep $it ms")
            Thread.sleep(it)
        }
        return "Hello world"
    }

    fun fallback(ex: Exception): String {
        return "⚠️ 서비스가 일시적으로 사용할 수 없습니다. (${ex.message})"
    }
}
