package com.raynor.demo.boiler.controller.order.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class CreateOrderRequestDto(
    @Valid val items: List<Item>,
    val couponId: Int?,
) {
    data class Item(
        val productId: Int,
        @Min(1) @Max(20)
        val quantity: Long,
    )
}
