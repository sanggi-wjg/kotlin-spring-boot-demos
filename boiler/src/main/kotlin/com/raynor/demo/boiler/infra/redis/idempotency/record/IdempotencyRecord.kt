package com.raynor.demo.boiler.infra.redis.idempotency.record

import java.time.Instant

data class IdempotencyRecord(
    val status: IdempotencyStatus,
    val requestBodyHash: String,
    val statusCode: Int?,
    val responseBody: String?,
    val createdAt: Instant,
    val completedAt: Instant?,
    val failedAt: Instant?,
) {
    companion object {
        fun processing(requestBodyHash: String) =
            IdempotencyRecord(
                status = IdempotencyStatus.PROCESSING,
                requestBodyHash = requestBodyHash,
                statusCode = null,
                responseBody = null,
                createdAt = Instant.now(),
                completedAt = null,
                failedAt = null,
            )
    }

    fun complete(
        statusCode: Int,
        responseBody: String?,
    ) = copy(
        status = IdempotencyStatus.COMPLETED,
        statusCode = statusCode,
        responseBody = responseBody,
        completedAt = Instant.now(),
        failedAt = null,
    )
}
