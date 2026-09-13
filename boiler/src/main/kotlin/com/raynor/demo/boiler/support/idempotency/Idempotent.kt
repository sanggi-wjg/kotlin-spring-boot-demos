package com.raynor.demo.boiler.support.idempotency

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Idempotent(
    val ttlSeconds: Long = 86_400, // 24 hours
    val procTtlSeconds: Long = 300,
)
