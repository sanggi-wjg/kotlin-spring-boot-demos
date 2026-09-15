package com.raynor.demo.boiler.controller.order.dto

import com.raynor.demo.boiler.domain.order.OrderStatus

data class OrderWebhookRequestDto(
    val orderId: Long,
    val status: OrderStatus,
)
