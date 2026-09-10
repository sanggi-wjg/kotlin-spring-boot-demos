package com.raynor.demo.boiler.service.product.model

import com.raynor.demo.boiler.domain.product.Product
import com.raynor.demo.boiler.domain.product.ProductStatus
import java.math.BigDecimal

data class ProductModel(
    val id: Int,
    val name: String,
    val price: BigDecimal,
    val stockQuantity: Long,
    val status: ProductStatus,
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
                status = entity.status,
                isSoldOut = entity.isSoldOut(),
                isSale = entity.isOnSale(),
            )
        }
    }
}
