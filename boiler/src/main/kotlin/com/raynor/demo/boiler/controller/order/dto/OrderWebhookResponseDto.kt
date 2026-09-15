package com.raynor.demo.boiler.controller.order.dto

import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.service.order.model.WebhookResult

data class OrderWebhookResponseDto(
    val orderId: Long,
    val orderStatus: OrderStatus?,
    val result: Result,
    val message: String? = null,
) {
    enum class Result {
        PROCESSED,
        ALREADY_PROCESSED,
        FAILED,
    }

    companion object {
        fun fromModel(model: WebhookResult) =
            OrderWebhookResponseDto(
                orderId = model.orderId,
                orderStatus = model.orderStatus,
                result = Result.valueOf(model.result.name),
                message = model.message,
            )
    }
}
