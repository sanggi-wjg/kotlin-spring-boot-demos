package com.raynor.demo.boiler.infra.redis.idempotency

import com.raynor.demo.boiler.infra.redis.idempotency.record.IdempotencyRecord
import com.raynor.demo.boiler.infra.redis.idempotency.record.IdempotencyStatus
import com.raynor.demo.boiler.support.idempotency.IdempotencyKeyMissingException
import com.raynor.demo.boiler.support.idempotency.Idempotent
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.redisson.api.RedissonClient
import org.redisson.client.codec.StringCodec
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
                ?: return ResponseEntity.status(HttpStatus.CONFLICT).body("Idempotency record is unavailable")
            return handleDuplicateRequest(requestBodyHash, bucketValue)
        }

        return try {
            val response = joinPoint.proceed() as ResponseEntity<*>
            val completeRecord = record.complete(
                response.statusCode.value(),
                response.body?.toString(),
            )
            bucket.set(objectMapper.writeValueAsString(completeRecord), Duration.ofSeconds(idempotent.ttlSeconds))
            response
        } catch (e: Exception) {
            log.error("Error executing idempotent method", e)
            bucket.delete()
            throw e
        }
    }

    private fun handleDuplicateRequest(
        requestBodyHash: String,
        bucketValue: String,
    ): ResponseEntity<*> {
        val record = objectMapper.readValue(bucketValue, IdempotencyRecord::class.java)
        if (record.requestBodyHash != requestBodyHash) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body("Idempotency-Key was used with a different request body")
        }

        return when (record.status) {
            IdempotencyStatus.PROCESSING -> {
                ResponseEntity.status(HttpStatus.CONFLICT).body("Request is already being processed")
            }

            IdempotencyStatus.COMPLETED -> {
                ResponseEntity.status(record.statusCode!!).body(record.responseBody)
            }
        }
    }

    private fun getIdempotencyKeyOrThrow(): String {
        return request.getHeader("Idempotency-Key")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw IdempotencyKeyMissingException("Idempotency-Key header is missing")
    }

    private fun getUserId(): String? {
        return request.getHeader("X-User-Id")?.trim()
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
        require(bodyIndex >= 0) { "RequestBody parameter is missing" }
        val requestBody = joinPoint.args[bodyIndex]

        return MessageDigest.getInstance("SHA-256")
            .digest(objectMapper.writeValueAsBytes(requestBody))
            .let { value -> HexFormat.of().formatHex(value) }
    }
}
