package com.raynor.demo.boiler.controller.order.dto

data class CreateOrderRequestDto(
    val items: List<Item>,
    val couponId: Int?,
) {
    data class Item(
        val productId: Int,
        val quantity: Long,
    )
}
