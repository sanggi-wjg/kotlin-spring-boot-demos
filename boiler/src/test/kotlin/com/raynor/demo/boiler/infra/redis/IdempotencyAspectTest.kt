package com.raynor.demo.boiler.infra.redis

import com.raynor.demo.boiler.infra.redis.idempotency.IdempotencyAspect
import com.raynor.demo.boiler.infra.redis.idempotency.record.IdempotencyRecord
import com.raynor.demo.boiler.infra.redis.idempotency.record.IdempotencyStatus
import com.raynor.demo.boiler.shared.http.ApiHeaders
import com.raynor.demo.boiler.shared.idempotency.IdempotencyKeyMissingException
import com.raynor.demo.boiler.shared.idempotency.IdempotencyKeyReusedException
import com.raynor.demo.boiler.shared.idempotency.IdempotencyRequestInProgressException
import com.raynor.demo.boiler.shared.idempotency.Idempotent
import com.raynor.demo.boiler.support.ServiceTestContext
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.redisson.api.RedissonClient
import org.redisson.client.codec.StringCodec
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.bind.annotation.RequestBody
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class IdempotencyAspectTest(
    private val redissonClient: RedissonClient,
) : ServiceTestContext(
        {

            val objectMapper = jacksonObjectMapper()

            fun request(
                idempotencyKey: String?,
                userId: String = "1",
            ) = MockHttpServletRequest("POST", "/api/v1/orders").apply {
                idempotencyKey?.let { addHeader(ApiHeaders.IDEMPOTENCY_KEY, it) }
                addHeader(ApiHeaders.USER_ID, userId)
            }

            // 실제 Redis 에 Aspect 를 붙인 프록시를 만든다. 호출 횟수는 프록시가 아닌 target 에서 확인한다.
            fun proxy(
                target: IdempotentTestTarget,
                request: MockHttpServletRequest,
            ): IdempotentTestTarget =
                AspectJProxyFactory(target)
                    .apply {
                        isProxyTargetClass = true
                        addAspect(IdempotencyAspect(request, objectMapper, redissonClient))
                    }.getProxy()

            test("첫 요청은 실행하고 응답을 COMPLETED 로 저장한다") {
                // given
                val key = UUID.randomUUID().toString()
                val target = IdempotentTestTarget()

                // when
                val response = proxy(target, request(key)).create(TestBody("a"))

                // then
                response.statusCode shouldBe HttpStatus.CREATED
                target.callCount.get() shouldBe 1

                val stored = redissonClient.getBucket<String>("POST:/api/v1/orders:users:1:$key", StringCodec.INSTANCE).get()
                objectMapper.readValue(stored, IdempotencyRecord::class.java).status shouldBe IdempotencyStatus.COMPLETED
            }

            test("완료된 요청을 다시 보내면 실행하지 않고 저장된 응답을 돌려준다") {
                // given
                val key = UUID.randomUUID().toString()
                val target = IdempotentTestTarget()
                val idempotentTarget = proxy(target, request(key))
                idempotentTarget.create(TestBody("a"))

                // when
                val response = idempotentTarget.create(TestBody("a"))

                // then
                response.statusCode shouldBe HttpStatus.CREATED
                response.headers.contentType shouldBe MediaType.APPLICATION_JSON
                response.body shouldBe objectMapper.writeValueAsString(TestResponse("a"))
                target.callCount.get() shouldBe 1
            }

            test("같은 멱등 키를 다른 본문에 쓰면 IdempotencyKeyReusedException 을 던진다") {
                // given
                val key = UUID.randomUUID().toString()
                val target = IdempotentTestTarget()
                val idempotentTarget = proxy(target, request(key))
                idempotentTarget.create(TestBody("a"))

                // when, then
                shouldThrow<IdempotencyKeyReusedException> {
                    idempotentTarget.create(TestBody("b"))
                }
                target.callCount.get() shouldBe 1
            }

            test("앞선 요청이 처리 중이면 IdempotencyRequestInProgressException 을 던진다") {
                // given
                val key = UUID.randomUUID().toString()
                val started = CountDownLatch(1)
                val release = CountDownLatch(1)
                val target = IdempotentTestTarget()
                target.handler = { body ->
                    started.countDown()
                    release.await(5, TimeUnit.SECONDS)
                    ResponseEntity.status(HttpStatus.CREATED).body(TestResponse(body.name))
                }
                val idempotentTarget = proxy(target, request(key))
                val executor = Executors.newSingleThreadExecutor()

                try {
                    val first = executor.submit(Callable { idempotentTarget.create(TestBody("a")) })
                    started.await(5, TimeUnit.SECONDS) shouldBe true

                    // when, then
                    shouldThrow<IdempotencyRequestInProgressException> {
                        idempotentTarget.create(TestBody("a"))
                    }

                    release.countDown()
                    first.get(5, TimeUnit.SECONDS).statusCode shouldBe HttpStatus.CREATED
                } finally {
                    release.countDown()
                    executor.shutdownNow()
                }
                target.callCount.get() shouldBe 1
            }

            test("실행 중 예외가 나면 멱등 키를 지워 같은 키로 다시 실행할 수 있다") {
                // given
                val key = UUID.randomUUID().toString()
                val target = IdempotentTestTarget()
                val idempotentTarget = proxy(target, request(key))
                target.handler = { throw IllegalStateException("재고 부족") }
                shouldThrow<IllegalStateException> {
                    idempotentTarget.create(TestBody("a"))
                }

                // when
                target.handler = IdempotentTestTarget.CREATED
                val response = idempotentTarget.create(TestBody("a"))

                // then
                response.statusCode shouldBe HttpStatus.CREATED
                target.callCount.get() shouldBe 2
            }

            test("다른 사용자는 같은 멱등 키를 써도 각각 실행된다") {
                // given
                val key = UUID.randomUUID().toString()
                val target = IdempotentTestTarget()

                // when
                proxy(target, request(key, userId = "1")).create(TestBody("a"))
                proxy(target, request(key, userId = "2")).create(TestBody("a"))

                // then
                target.callCount.get() shouldBe 2
            }

            test("멱등 키 헤더가 없으면 IdempotencyKeyMissingException 을 던진다") {
                // given
                val target = IdempotentTestTarget()

                // when, then
                shouldThrow<IdempotencyKeyMissingException> {
                    proxy(target, request(null)).create(TestBody("a"))
                }
                target.callCount.get() shouldBe 0
            }
        },
    )

data class TestBody(
    val name: String,
)

data class TestResponse(
    val name: String,
)

open class IdempotentTestTarget {
    val callCount = AtomicInteger()
    var handler: (TestBody) -> ResponseEntity<*> = CREATED

    @Idempotent
    open fun create(
        @RequestBody body: TestBody,
    ): ResponseEntity<*> {
        callCount.incrementAndGet()
        return handler(body)
    }

    companion object {
        val CREATED: (TestBody) -> ResponseEntity<*> = { body ->
            ResponseEntity.status(HttpStatus.CREATED).body(TestResponse(body.name))
        }
    }
}
