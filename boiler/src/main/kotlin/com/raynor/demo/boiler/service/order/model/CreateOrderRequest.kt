package com.raynor.demo.boiler.service.order.model

data class CreateOrderRequest(
    val items: List<Item>,
    val couponId: Int?,
) {
    data class Item(
        val productId: Int,
        val quantity: Long,
    )

    init {
        require(items.isNotEmpty()) { "주문 상품이 비어 있습니다." }
        require(items.distinctBy { it.productId }.size == items.size) { "중복 상품 ID가 발견되었습니다." }
        require(items.all { it.quantity in 1..20 }) { "주문 수량이 1보다 작거나 20보다 큽니다." }
    }
}
