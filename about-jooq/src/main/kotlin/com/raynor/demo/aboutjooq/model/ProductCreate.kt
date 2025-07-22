package com.raynor.demo.aboutjooq.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductCreate(
    val name: String,
    val memo: String?,
    val price: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
