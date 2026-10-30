package com.raynor.demo.boiler.infra.redis.idempotency.record

import java.time.Instant

data class IdempotencyRecord(
    val status: IdempotencyStatus,
    val requestBodyHash: String,
    val statusCode: Int?,
    val responseBody: String?,
    val responseHeaders: Map<String, List<String>>?,
    val createdAt: Instant,
    val completedAt: Instant?,
) {
    companion object {
        fun processing(requestBodyHash: String) =
            IdempotencyRecord(
                status = IdempotencyStatus.PROCESSING,
                requestBodyHash = requestBodyHash,
                statusCode = null,
                responseBody = null,
                responseHeaders = null,
                createdAt = Instant.now(),
                completedAt = null,
            )
    }

    fun complete(
        statusCode: Int,
        responseBody: String?,
        responseHeaders: Map<String, List<String>>?,
    ) = copy(
        status = IdempotencyStatus.COMPLETED,
        statusCode = statusCode,
        responseBody = responseBody,
        responseHeaders = responseHeaders,
        completedAt = Instant.now(),
    )
}
