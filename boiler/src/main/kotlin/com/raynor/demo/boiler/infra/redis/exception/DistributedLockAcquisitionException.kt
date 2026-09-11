package com.raynor.demo.boiler.infra.redis.exception

class DistributedLockAcquisitionException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
