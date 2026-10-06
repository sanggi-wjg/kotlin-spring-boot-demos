package com.raynor.demo.boiler.infra.redis.lock

import com.raynor.demo.boiler.infra.redis.exception.DistributedLockAcquisitionException
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.redisson.client.RedisException
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
        require(keys.isNotEmpty()) { "분산락 키가 비어 있습니다." }

        val lockKeys = keys.distinct().sorted().map { "lock:$it" }
        val lockName = lockKeys.joinToString(",")

        val locks = lockKeys.map { redissonClient.getLock(it) }
        val lock = locks.singleOrNull()
            ?: redissonClient.getMultiLock(*locks.toTypedArray())

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
            log.debug("분산락 획득: {}", lockName)
            return action()
        } finally {
            locks.forEach { unlockQuietly(it) }
        }
    }

    private fun unlockQuietly(lock: RLock) {
        try {
            lock.unlock()
            log.debug("분산락 해제: {}", lock.name)
        } catch (exception: IllegalMonitorStateException) {
            log.error("분산락 소유권 상실, 작업 중 락이 만료됐을 수 있음: {}", lock.name, exception)
        } catch (exception: RedisException) {
            log.error("분산락 해제 실패, watchdog 만료 후 자동 해제됨: {}", lock.name, exception)
        } catch (exception: Exception) {
            log.error("분산락 해제 실패: {}", lock.name, exception)
        }
    }
}
