package com.raynor.demo.boiler.controller.order.dto

import com.raynor.demo.boiler.service.order.model.OrderModel

data class OrderResponseDto(
    val id: Long,
) {
    companion object {
        fun fromModel(model: OrderModel) =
            OrderResponseDto(
                id = model.id,
            )
    }
}
