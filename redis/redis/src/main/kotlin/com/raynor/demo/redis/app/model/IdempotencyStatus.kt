package com.raynor.demo.redis.app.model

enum class IdempotencyStatus {
    ALREADY_REQUESTED,
    ALREADY_DONE,
    SUCCESS
}
