package com.raynor.demo.boiler.infra.redis.idempotency.record

enum class IdempotencyStatus {
    PROCESSING,
    COMPLETED,
}
