package com.raynor.demo.productservice.service.model.command

import com.raynor.demo.productservice.rds.entity.ProductEntity
import com.raynor.demo.shared.typed.Money
import com.raynor.demo.shared.typed.product.ProductName
import com.raynor.demo.shared.typed.product.ProductStockQuantity

data class CreateProductCommand(
    val name: ProductName,
    val price: Money,
    val stockQuantity: ProductStockQuantity,
) {
    fun toEntity() = ProductEntity(
        name = name.value,
        price = price.value,
        stockQuantity = stockQuantity.value,
    )
}
