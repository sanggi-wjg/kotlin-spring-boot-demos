package com.raynor.demo.boiler.service.product.model

import com.raynor.demo.boiler.domain.product.Product
import java.math.BigDecimal

data class ProductModel(
    val id: Int,
    val name: String,
    val price: BigDecimal,
    val stockQuantity: Long,
    val status: String,
    val isSoldOut: Boolean,
    val isSale: Boolean,
) {
    companion object {
        fun fromEntity(entity: Product): ProductModel {
            return ProductModel(
                id = entity.id!!,
                name = entity.name,
                price = entity.price.amount,
                stockQuantity = entity.stock.quantity,
                status = entity.status.name,
                isSoldOut = entity.isStockStatusOutOfStock(),
                isSale = entity.isSale(),
            )
        }
    }
}
