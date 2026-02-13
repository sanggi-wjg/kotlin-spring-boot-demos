package com.raynor.demo.productservice.service.model

import com.raynor.demo.productservice.rds.entity.ProductEntity
import com.raynor.demo.shared.typed.Money
import com.raynor.demo.shared.typed.product.*
import com.raynor.demo.shared.typed.toMoney

data class Product(
    val id: ProductId,
    val name: ProductName,
    val price: Money,
    val stockQuantity: ProductStockQuantity,
) {
    companion object
}

fun ProductEntity.toModel() = Product(
    id = id!!.toProductId(),
    name = name.toProductName(),
    price = price.toMoney(),
    stockQuantity = stockQuantity.toProductStockQuantity(),
)
