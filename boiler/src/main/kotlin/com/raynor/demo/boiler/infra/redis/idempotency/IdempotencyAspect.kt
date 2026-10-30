package com.raynor.demo.boiler.infra.redis.idempotency

import com.raynor.demo.boiler.infra.redis.idempotency.record.IdempotencyRecord
import com.raynor.demo.boiler.infra.redis.idempotency.record.IdempotencyStatus
import com.raynor.demo.boiler.shared.http.ApiHeaders
import com.raynor.demo.boiler.shared.idempotency.IdempotencyKeyMissingException
import com.raynor.demo.boiler.shared.idempotency.IdempotencyKeyReusedException
import com.raynor.demo.boiler.shared.idempotency.IdempotencyRequestInProgressException
import com.raynor.demo.boiler.shared.idempotency.Idempotent
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.redisson.api.RedissonClient
import org.redisson.client.codec.StringCodec
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import tools.jackson.databind.ObjectMapper
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat

@Aspect
@Component
class IdempotencyAspect(
    private val request: HttpServletRequest,
    private val objectMapper: ObjectMapper,
    private val redissonClient: RedissonClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Around("@annotation(idempotent)")
    fun around(
        joinPoint: ProceedingJoinPoint,
        idempotent: Idempotent,
    ): ResponseEntity<*> {
        val idempotencyKey = getIdempotencyKeyOrThrow()
        val userId = getUserId()
        val redisKey = buildRedisKey(idempotencyKey, userId)
        val requestBodyHash = getRequestBodyHash(joinPoint)

        val bucket = redissonClient.getBucket<String>(redisKey, StringCodec.INSTANCE)
        val record = IdempotencyRecord.processing(requestBodyHash)

        val acquired = bucket.setIfAbsent(
            objectMapper.writeValueAsString(record),
            Duration.ofSeconds(idempotent.procTtlSeconds),
        )
        if (!acquired) {
            val bucketValue = bucket.get()
                ?: throw IdempotencyRequestInProgressException("요청 처리 상태를 확인할 수 없습니다. 잠시 후 다시 시도해 주세요.")
            return handleDuplicateRequest(requestBodyHash, bucketValue)
        }

        val response = try {
            joinPoint.proceed() as ResponseEntity<*>
        } catch (e: Throwable) {
            log.info("멱등 요청 처리 실패로 멱등 키 삭제: {}", redisKey)
            runCatching { bucket.delete() }.onFailure { e.addSuppressed(it) }
            throw e
        }

        runCatching {
            val completeRecord = record.complete(
                statusCode = response.statusCode.value(),
                responseBody = objectMapper.writeValueAsString(response.body),
                responseHeaders = buildMap { response.headers.forEach { key, value -> put(key, value) } },
            )
            bucket.set(objectMapper.writeValueAsString(completeRecord), Duration.ofSeconds(idempotent.ttlSeconds))
        }.onFailure {
            log.error("멱등 응답 저장 실패, 처리 중 상태로 TTL 까지 유지됨: {}", redisKey, it)
        }
        return response
    }

    private fun handleDuplicateRequest(
        requestBodyHash: String,
        bucketValue: String,
    ): ResponseEntity<*> {
        val record = objectMapper.readValue(bucketValue, IdempotencyRecord::class.java)
        if (record.requestBodyHash != requestBodyHash) {
            throw IdempotencyKeyReusedException("${ApiHeaders.IDEMPOTENCY_KEY} 가 다른 요청 본문으로 이미 사용되었습니다.")
        }

        return when (record.status) {
            IdempotencyStatus.PROCESSING -> {
                throw IdempotencyRequestInProgressException("동일한 요청을 처리하고 있습니다.")
            }

            IdempotencyStatus.COMPLETED -> {
                ResponseEntity.status(record.statusCode!!)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers { headers ->
                        record.responseHeaders?.forEach { (key, value) -> headers.addAll(key, value) }
                    }
                    .body(record.responseBody)
            }
        }
    }

    private fun getIdempotencyKeyOrThrow(): String {
        return request.getHeader(ApiHeaders.IDEMPOTENCY_KEY)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw IdempotencyKeyMissingException("${ApiHeaders.IDEMPOTENCY_KEY} 헤더가 필요합니다.")
    }

    private fun getUserId(): String? {
        return request.getHeader(ApiHeaders.USER_ID)?.trim()
    }

    private fun buildRedisKey(
        idempotencyKey: String,
        userId: String?,
    ): String {
        return "${request.method}:${request.requestURI}:users:$userId:$idempotencyKey"
    }

    private fun getRequestBodyHash(joinPoint: ProceedingJoinPoint): String {
        val signature = joinPoint.signature as MethodSignature
        val bodyIndex = signature.method.parameterAnnotations.indexOfFirst { annotations ->
            annotations.any { annotation -> annotation is RequestBody }
        }
        // 바디가 없는 API 는 요청 URI 가 Redis 키에 포함되어 요청을 구분하므로 빈 바디로 해시한다
        val requestBodyBytes = if (bodyIndex >= 0) {
            objectMapper.writeValueAsBytes(joinPoint.args[bodyIndex])
        } else {
            ByteArray(0)
        }

        return MessageDigest.getInstance("SHA-256")
            .digest(requestBodyBytes)
            .let { value -> HexFormat.of().formatHex(value) }
    }
}
