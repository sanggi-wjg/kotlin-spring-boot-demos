package com.raynor.demo.redis.app.model

enum class IdempotenceStatus {
    ALREADY_REQUESTED,
    ALREADY_DONE,
    SUCCESS
}