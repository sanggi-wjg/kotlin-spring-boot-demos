package com.raynor.demo.orderservice.service.model.command

import java.math.BigDecimal

data class CreateOrderCommand(
    val productId: Long,
    val quantity: Int,
    val amount: BigDecimal,
)