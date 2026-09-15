package com.raynor.demo.boiler.domain.order

import com.raynor.demo.boiler.domain.product.Product

data class OrderItemLine(
    val product: Product,
    val quantity: Long,
)
