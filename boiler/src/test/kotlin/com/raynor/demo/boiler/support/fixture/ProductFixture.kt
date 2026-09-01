package com.raynor.demo.boiler.support.fixture

import com.raynor.demo.boiler.domain.product.Product
import com.raynor.demo.boiler.domain.support.toMoney
import com.raynor.demo.boiler.domain.support.toStock

object ProductFixture {
    fun general(
        name: String = "테스트 상품",
        price: String = "1000",
        stockQuantity: Long = 1L,
    ) = Product(
        name = name,
        price = price.toMoney(),
        stock = stockQuantity.toStock(),
    )
}
