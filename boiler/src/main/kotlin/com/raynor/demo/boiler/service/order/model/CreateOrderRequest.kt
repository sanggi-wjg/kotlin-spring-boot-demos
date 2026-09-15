package com.raynor.demo.boiler.service.order.model

data class CreateOrderRequest(
    val items: List<Item>,
    val couponId: Int?,
) {
    data class Item(
        val productId: Int,
        val quantity: Long,
    )
}
