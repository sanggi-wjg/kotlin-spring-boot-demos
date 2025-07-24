package com.raynor.demo.aboutjooq.model

import java.math.BigDecimal

data class ProductUpdate(
    val name: String,
    val memo: String?,
    val price: BigDecimal,
)
