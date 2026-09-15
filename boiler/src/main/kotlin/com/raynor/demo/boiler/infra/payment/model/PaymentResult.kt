package com.raynor.demo.boiler.infra.payment.model

import com.raynor.demo.boiler.domain.order.OrderStatus

data class PaymentResult(
    val orderId: Long,
    val status: OrderStatus,
)
