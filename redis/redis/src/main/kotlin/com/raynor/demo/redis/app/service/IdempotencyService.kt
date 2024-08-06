package com.raynor.demo.redis.app.service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ValueOperations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class IdempotencyService(
    private val valueOps: ValueOperations<String, String>,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // RedisTemplate this.setEnableTransactionSupport(true)
    @Transactional
    fun <T> validateIdempotency(idempotencyKey: String, function: () -> T): ResponseEntity<T> {
        val requestedKey = "idempotency-requested:$idempotencyKey"
        val doneKey = "idempotency-done:$idempotencyKey"
        val defaultPolicyOfTTL = Duration.ofSeconds(60)

        if (valueOps.get(doneKey) == "1") {
            logger.warn("$idempotencyKey is done")
            return ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY)
        }

        if (valueOps.get(requestedKey) == "1") {
            logger.warn("$idempotencyKey is requested")
            return ResponseEntity(HttpStatus.CONFLICT)
        }

        logger.info("$idempotencyKey is processing")
        valueOps.set(requestedKey, "1", defaultPolicyOfTTL)

        return ResponseEntity.ok(function.invoke()).also {
            logger.info("$idempotencyKey is finished")
            valueOps.getAndDelete(requestedKey)
            valueOps.set(doneKey, "1", defaultPolicyOfTTL)
        }
    }
}
