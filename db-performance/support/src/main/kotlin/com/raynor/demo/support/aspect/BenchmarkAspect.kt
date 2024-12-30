package com.raynor.demo.support.aspect

import com.raynor.demo.support.annotation.Benchmark
import com.raynor.demo.support.config.Constants.CYCLE_COUNT
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Aspect
@Component
class BenchmarkAspect {
    private val logger = LoggerFactory.getLogger(this::class.java)

    //    @Around("execution(* com.raynor.demo.dbmysqlonly.service.*.*(..)) || execution(* com.raynor.demo.dbvendor.service.*.*(..))")
    @Around("@annotation(benchmark)")
    fun benchmarkAroundAdvice(joinPoint: ProceedingJoinPoint, benchmark: Benchmark) {
        val times = mutableListOf<Long>()

        repeat(CYCLE_COUNT) {
            val startedAt = Instant.now()
            joinPoint.proceed()
            times.add(Instant.now().toEpochMilli() - startedAt.toEpochMilli())
        }
        logger.info("== ${joinPoint.target::class.java.simpleName}::${joinPoint.signature.name}\n$times, Average: ${times.average()} ms")
    }
}