package com.raynor.demo.boiler.support.idempotency

class IdempotencyKeyMissingException(
    message: String,
) : RuntimeException(message)
