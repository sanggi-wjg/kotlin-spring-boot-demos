package com.raynor.demo.boiler.shared.idempotency

class IdempotencyKeyReusedException(
    message: String,
) : RuntimeException(message)
