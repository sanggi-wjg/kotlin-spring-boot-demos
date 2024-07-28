package com.raynor.demo.redis.app.model

import java.math.BigDecimal
import java.time.Instant

data class Something(
    val id: Int,
    val name: String,
    val amount: BigDecimal,
    val status: SomethingStatus,
    val createdAt: Instant,
)

enum class SomethingStatus {
    ACTIVE, INACTIVE
}


data class UniqueData(
    val uniqueId: Int,
    val name: String
)