package com.raynor.demo.boiler.service.lab

import org.springframework.dao.CannotAcquireLockException
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class RollbackOnTimeoutFacadeService(
    private val rollbackOnTimeoutService: RollbackOnTimeoutService,
) {
    @Retryable(
        includes = [CannotAcquireLockException::class],
        maxRetries = 3,
        delay = 50L,
    )
    fun transactionB(): Map<String, String?> {
        return rollbackOnTimeoutService.transactionB()
    }
}
