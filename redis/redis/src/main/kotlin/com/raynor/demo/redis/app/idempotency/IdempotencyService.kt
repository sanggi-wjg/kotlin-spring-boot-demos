package com.raynor.demo.redis.app.idempotency

import com.raynor.demo.redis.app.model.IdempotencyStatus
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class IdempotencyService(
    private val valueOps: ValueOperations<String, String>,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun <T> validateIdempotency(idempotencyKey: String, function: () -> T): Pair<T?, IdempotencyStatus> {
        val requestedKey = "idempotency-requested:$idempotencyKey"
        val doneKey = "idempotency-done:$idempotencyKey"
        val defaultPolicyOfTTL = Duration.ofSeconds(60)

        if (valueOps.get(doneKey) == "1") {
            logger.info("$idempotencyKey is done")
            return Pair(null, IdempotencyStatus.ALREADY_DONE)
        }

        if (valueOps.get(requestedKey) == "1") {
            logger.info("$idempotencyKey is requested")
            return Pair(null, IdempotencyStatus.ALREADY_REQUESTED)
        }

        logger.info("$idempotencyKey processing")
        valueOps.set(requestedKey, "1", defaultPolicyOfTTL)

        return Pair(function.invoke(), IdempotencyStatus.SUCCESS).also {
            logger.info("$idempotencyKey done")
            valueOps.getAndDelete(requestedKey)
            valueOps.set(doneKey, "1", defaultPolicyOfTTL)
        }
    }
}
