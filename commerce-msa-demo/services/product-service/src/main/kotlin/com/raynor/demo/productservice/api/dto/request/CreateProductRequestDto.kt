package com.raynor.demo.productservice.api.dto.request

import com.raynor.demo.shared.typed.Money
import com.raynor.demo.shared.typed.product.ProductName
import com.raynor.demo.shared.typed.product.ProductStockQuantity

data class CreateProductRequestDto(
    val name: ProductName,
    val price: Money,
    val stockQuantity: ProductStockQuantity,
) {
    companion object
}