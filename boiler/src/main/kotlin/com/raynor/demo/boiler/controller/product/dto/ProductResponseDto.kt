package com.raynor.demo.boiler.controller.product.dto

import com.raynor.demo.boiler.service.product.model.ProductModel
import java.math.BigDecimal

data class ProductResponseDto(
    val id: Int,
    val name: String,
    val price: BigDecimal,
    val stockQuantity: Long,
    val isSoldOut: Boolean,
    val isSale: Boolean,
) {
    companion object {
        fun fromModel(model: ProductModel) =
            ProductResponseDto(
                id = model.id,
                name = model.name,
                price = model.price,
                stockQuantity = model.stockQuantity,
                isSoldOut = model.isSoldOut,
                isSale = model.isSale,
            )
    }
}
