package com.raynor.demo.boiler.service.product.model

import com.raynor.demo.boiler.domain.product.Product
import java.math.BigDecimal

data class ProductModel(
    val id: Int,
    val name: String,
    val price: BigDecimal,
    val stockQuantity: Long,
    val isSale: Boolean,
) {
    companion object {
        fun fromEntity(entity: Product): ProductModel {
            return ProductModel(
                id = entity.id!!,
                name = entity.name,
                price = entity.price,
                stockQuantity = entity.stockQuantity,
                isSale = entity.isSale(),
            )
        }
    }
}
