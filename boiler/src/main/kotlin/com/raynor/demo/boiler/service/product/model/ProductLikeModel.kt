package com.raynor.demo.boiler.service.product.model

data class ProductLikeModel(
    val productId: Int,
    val productLikeCount: Long,
    val isLiked: Boolean,
)
