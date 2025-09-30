package com.raynor.demo.shared.typed.product

@JvmInline
value class ProductStockQuantity(val value: Int) {
    init {
        require(value >= 0) { "상품 수량은 0보다 작을 수 없습니다." }
    }

    operator fun plus(other: ProductStockQuantity): ProductStockQuantity {
        return ProductStockQuantity(this.value + other.value)
    }

    operator fun minus(other: ProductStockQuantity): ProductStockQuantity {
        return ProductStockQuantity(this.value - other.value)
    }
}
