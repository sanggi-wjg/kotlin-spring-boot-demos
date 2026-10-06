package com.raynor.demo.boiler.infra.redis

import com.raynor.demo.boiler.infra.redis.exception.DistributedLockAcquisitionException
import com.raynor.demo.boiler.infra.redis.lock.DistributedLockExecutor
import com.raynor.demo.boiler.support.ServiceTestContext
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.redisson.api.RLock
import org.redisson.api.RedissonClient

class DistributedLockExecutorTest(
    private val lockExecutor: DistributedLockExecutor,
) : ServiceTestContext(
        {

            val key = "test"
            val mockClient = mockk<RedissonClient>()
            val mockLock = mockk<RLock>()

            test("락 획득 후 해제") {
                lockExecutor.execute(key, 5L, action = { })
            }

            test("락 획득중 인터럽트 발생") {
                // given
                val executor = DistributedLockExecutor(mockClient)

                // mock
                every { mockClient.getLock(any<String>()) } returns mockLock
                every { mockLock.tryLock(any(), any()) } throws InterruptedException()

                // when, then
                try {
                    shouldThrow<DistributedLockAcquisitionException> {
                        executor.execute(key, 5L, action = { })
                    }
                    Thread.currentThread().isInterrupted shouldBe true
                } finally {
                    Thread.interrupted()
                }

                verify(exactly = 0) { mockLock.unlock() }
            }

            test("락 획득 실패") {
                // given
                val executor = DistributedLockExecutor(mockClient)

                // mock
                every { mockClient.getLock(any<String>()) } returns mockLock
                every { mockLock.tryLock(any(), any()) } returns false

                // when, then
                shouldThrow<DistributedLockAcquisitionException> {
                    executor.execute(key, 5L, action = { })
                }

                verify(exactly = 0) { mockLock.unlock() }
            }

            context("다중 키") {

                test("여러 키 락 획득 후 해제") {
                    var executed = false

                    lockExecutor.execute(listOf("product:1", "product:2"), 5L, action = { executed = true })

                    executed shouldBe true
                }

                test("단일 키는 MultiLock 을 만들지 않는다") {
                    // given
                    val executor = DistributedLockExecutor(mockClient)

                    // mock
                    every { mockClient.getLock(any<String>()) } returns mockLock
                    every { mockLock.tryLock(any(), any()) } returns true
                    every { mockLock.unlock() } returns Unit
                    every { mockLock.name } returns "lock:$key"

                    // when
                    executor.execute(listOf(key), 5L, action = { })

                    // then
                    verify(exactly = 0) { mockClient.getMultiLock(*anyVararg()) }
                    verify(exactly = 1) { mockLock.unlock() }
                }

                test("여러 키는 정렬된 순서로 MultiLock 을 만들고 개별 락을 각각 해제한다") {
                    // given
                    val executor = DistributedLockExecutor(mockClient)
                    val lockA = mockk<RLock>()
                    val lockB = mockk<RLock>()
                    val mockMultiLock = mockk<RLock>()

                    // mock
                    every { mockClient.getLock("lock:a") } returns lockA
                    every { mockClient.getLock("lock:b") } returns lockB
                    every { mockClient.getMultiLock(lockA, lockB) } returns mockMultiLock
                    every { mockMultiLock.tryLock(any(), any()) } returns true
                    listOf(lockA, lockB).forEach {
                        every { it.unlock() } returns Unit
                        every { it.name } returns "lock"
                    }

                    // when
                    executor.execute(listOf("b", "a", "b"), 5L, action = { })

                    // then
                    verify(exactly = 1) { mockClient.getMultiLock(lockA, lockB) }
                    verify(exactly = 1) { lockA.unlock() }
                    verify(exactly = 1) { lockB.unlock() }
                    verify(exactly = 0) { mockMultiLock.unlock() }
                }

                test("빈 키 목록은 거부한다") {
                    shouldThrow<IllegalArgumentException> {
                        lockExecutor.execute(emptyList(), 5L, action = { })
                    }
                }
            }

            context("락 해제 실패") {

                test("해제 중 예외가 나도 작업 결과를 반환하고 나머지 락도 해제한다") {
                    // given
                    val executor = DistributedLockExecutor(mockClient)
                    val lockA = mockk<RLock>()
                    val lockB = mockk<RLock>()
                    val mockMultiLock = mockk<RLock>()

                    // mock
                    every { mockClient.getLock("lock:a") } returns lockA
                    every { mockClient.getLock("lock:b") } returns lockB
                    every { mockClient.getMultiLock(lockA, lockB) } returns mockMultiLock
                    every { mockMultiLock.tryLock(any(), any()) } returns true
                    every { lockA.unlock() } throws IllegalMonitorStateException()
                    every { lockB.unlock() } returns Unit
                    listOf(lockA, lockB).forEach { every { it.name } returns "lock" }

                    // when
                    val result = executor.execute(listOf("a", "b"), 5L, action = { "done" })

                    // then
                    result shouldBe "done"
                    verify(exactly = 1) { lockB.unlock() }
                }

                test("여러 키 락 획득 실패") {
                    // given
                    val executor = DistributedLockExecutor(mockClient)
                    val mockMultiLock = mockk<RLock>()

                    // mock
                    every { mockClient.getLock(any<String>()) } returns mockLock
                    every { mockClient.getMultiLock(*anyVararg()) } returns mockMultiLock
                    every { mockMultiLock.tryLock(any(), any()) } returns false

                    // when, then
                    shouldThrow<DistributedLockAcquisitionException> {
                        executor.execute(listOf("a", "b"), 5L, action = { })
                    }

                    verify(exactly = 0) { mockMultiLock.unlock() }
                }
            }
        },
    )
