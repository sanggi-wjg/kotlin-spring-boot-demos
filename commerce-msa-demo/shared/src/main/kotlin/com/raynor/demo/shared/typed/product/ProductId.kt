package com.raynor.demo.shared.typed.product

@JvmInline
value class ProductId(val value: Long) {
    init {
        require(value > 0) { "Product ID는 0보다 커야 합니다" }
    }
}

fun Long.toProductId() = ProductId(this)
