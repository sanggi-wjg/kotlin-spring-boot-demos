package com.raynor.demo.boiler.shared.idempotency

class IdempotencyKeyMissingException(
    message: String,
) : RuntimeException(message)
