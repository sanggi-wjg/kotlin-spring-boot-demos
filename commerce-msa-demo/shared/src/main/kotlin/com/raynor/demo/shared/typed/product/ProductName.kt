package com.raynor.demo.shared.typed.product

@JvmInline
value class ProductName(val value: String) {
    init {
        require(this.value.isNotBlank()) { "상품명은 비어있을 수 없습니다" }
        require(this.value.length <= 100) { "상품명은 100자를 초과할 수 없습니다" }
    }
}

fun String.toProductName() = ProductName(this)
