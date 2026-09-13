package com.raynor.demo.boiler.shared.idempotency

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Idempotent(
    val ttlSeconds: Long = 86_400, // 24 hours
    val procTtlSeconds: Long = 300,
)
