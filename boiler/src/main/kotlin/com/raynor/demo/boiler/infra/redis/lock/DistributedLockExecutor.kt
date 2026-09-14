package com.raynor.demo.boiler.infra.redis.lock

import com.raynor.demo.boiler.infra.redis.exception.DistributedLockAcquisitionException
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class DistributedLockExecutor(
    private val redissonClient: RedissonClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun <T> execute(
        key: String,
        waitTime: Long,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        action: () -> T,
    ): T = execute(listOf(key), waitTime, timeUnit, action)

    fun <T> execute(
        keys: List<String>,
        waitTime: Long,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        action: () -> T,
    ): T {
        val lockKeys = keys.distinct().map { "lock:$it" }
        val lockName = lockKeys.joinToString(",")
        val lock = if (lockKeys.size == 1) {
            redissonClient.getLock(lockKeys.single())
        } else {
            redissonClient.getMultiLock(*lockKeys.map { redissonClient.getLock(it) }.toTypedArray())
        }

        val acquired = try {
            lock.tryLock(waitTime, timeUnit)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw DistributedLockAcquisitionException("분산락 대기 중 인터럽트 발생: $lockName", exception)
        }

        if (!acquired) {
            throw DistributedLockAcquisitionException("분산락 획득 실패: $lockName")
        }

        try {
            log.info("분산락 획득: {}", lockName)
            return action()
        } finally {
            try {
                lock.unlock()
                log.info("분산락 해제: {}", lockName)
            } catch (exception: IllegalMonitorStateException) {
                log.warn("분산락 소유권 이미 해제: {}", lockName)
            }
        }
    }
}
