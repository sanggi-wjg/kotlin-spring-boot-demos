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
    ): T {
        val lockKey = "lock:$key"
        val lock = redissonClient.getLock(lockKey)

        val acquired = try {
            lock.tryLock(waitTime, timeUnit)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw DistributedLockAcquisitionException("분산락 대기 중 인터럽트 발생: $lockKey", exception)
        }

        if (!acquired) {
            throw DistributedLockAcquisitionException("분산락 획득 실패: $lockKey")
        }

        try {
            log.debug("분산락 획득: $lockKey")
            return action()
        } finally {
            try {
                lock.unlock()
                log.debug("분산락 해제: $lockKey")
            } catch (exception: IllegalMonitorStateException) {
                log.warn("분산락 소유권 이미 해제: $lockKey")
            }
        }
    }
}
