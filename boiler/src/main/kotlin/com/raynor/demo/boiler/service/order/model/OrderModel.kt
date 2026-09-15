package com.raynor.demo.boiler.service.order.model

import com.raynor.demo.boiler.domain.order.Order

data class OrderModel(
    val id: Long,
) {
    companion object {
        fun fromEntity(order: Order): OrderModel {
            return OrderModel(
                id = order.id!!,
            )
        }
    }
}
