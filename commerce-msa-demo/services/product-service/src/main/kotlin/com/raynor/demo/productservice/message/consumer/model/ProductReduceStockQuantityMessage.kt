package com.raynor.demo.productservice.message.consumer.model

data class ProductReduceStockQuantityMessage(
    val productId: Long,
    val quantity: Int,
)