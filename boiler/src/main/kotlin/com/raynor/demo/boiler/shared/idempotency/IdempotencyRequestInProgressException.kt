package com.raynor.demo.boiler.shared.idempotency

class IdempotencyRequestInProgressException(
    message: String,
) : RuntimeException(message)
