package com.raynor.demo.boiler.service.order.model

import com.raynor.demo.boiler.domain.order.OrderStatus

data class OrderWebhookRequest(
    val orderId: Long,
    val status: OrderStatus,
)
