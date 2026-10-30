package com.raynor.demo.boiler.shared.exception

class DistributedLockAcquisitionException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
