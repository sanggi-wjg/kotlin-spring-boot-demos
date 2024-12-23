package com.raynor.demo.aboutcircuitbreaker

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.stereotype.Service
import java.lang.Exception
import kotlin.random.Random

@Service
class IamService {

    @CircuitBreaker(name = "iamService", fallbackMethod = "fallback")
    fun whoAmI(): String {
        Thread.sleep(Random.nextLong(1_000, 20_000))
        if (Random.nextBoolean()) {
            throw RuntimeException("something wrong")
        }
        return "Hello world"
    }

    fun fallback(ex: Exception): String {
        return "⚠️ 서비스가 일시적으로 사용할 수 없습니다. (${ex.message})"
    }
}
