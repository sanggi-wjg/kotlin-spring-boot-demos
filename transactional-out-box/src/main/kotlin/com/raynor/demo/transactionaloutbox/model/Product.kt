package com.raynor.demo.transactionaloutbox.model

import java.math.BigDecimal
import java.time.Instant

data class Product(
    val id: Int,
    val name: String,
    val price: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
)
