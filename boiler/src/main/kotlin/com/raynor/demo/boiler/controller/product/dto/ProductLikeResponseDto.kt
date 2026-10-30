package com.raynor.demo.boiler.controller.product.dto

import com.raynor.demo.boiler.service.product.model.ProductLikeModel

data class ProductLikeResponseDto(
    val productId: Int,
    val productLikeCount: Long,
    val isLiked: Boolean,
) {
    companion object {
        fun fromModel(productLike: ProductLikeModel) =
            ProductLikeResponseDto(
                productId = productLike.productId,
                productLikeCount = productLike.productLikeCount,
                isLiked = productLike.isLiked,
            )
    }
}
