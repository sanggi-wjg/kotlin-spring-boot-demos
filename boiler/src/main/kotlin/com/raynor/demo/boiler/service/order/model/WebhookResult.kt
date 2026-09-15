package com.raynor.demo.boiler.service.order.model

import com.raynor.demo.boiler.domain.order.OrderStatus

data class WebhookResult(
    val orderId: Long,
    /** 처리 후 주문 상태. 주문을 찾지 못했거나 PG 검증 단계에서 끝난 경우 null */
    val orderStatus: OrderStatus?,
    val result: Result,
    val message: String? = null,
) {
    enum class Result {
        PROCESSED,
        ALREADY_PROCESSED,
        FAILED,
    }
}
