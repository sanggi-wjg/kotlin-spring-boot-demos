package com.raynor.demo.dbperformance.aspect

import com.raynor.demo.dbperformance.config.Constants.PERFORMANCE_TEST_CYCLE_COUNT
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Aspect
@Component
class PerformanceAspect {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Around("execution(* com.raynor.demo.dbperformance.service.*.*(..))")
    fun performanceAroundAdvice(joinPoint: ProceedingJoinPoint) {
        val times = mutableListOf<Long>()

        repeat(PERFORMANCE_TEST_CYCLE_COUNT) {
            val startedAt = Instant.now()
            val result = joinPoint.proceed()
            times.add(Instant.now().toEpochMilli() - startedAt.toEpochMilli())
        }
        logger.info("== ${joinPoint.target::class.java.simpleName}::${joinPoint.signature.name}\nTimes: $times, Average: ${times.average()} ms")

//        val startedAt = Instant.now()
//        val result = joinPoint.proceed()
//        val elapsed = Instant.now().toEpochMilli() - startedAt.toEpochMilli()
//        logger.info("== ${joinPoint.target::class.java.simpleName}::${joinPoint.signature.name} took $elapsed ms")
//        return result
    }
}