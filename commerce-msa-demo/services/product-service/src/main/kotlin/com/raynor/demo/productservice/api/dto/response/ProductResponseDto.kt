package com.raynor.demo.productservice.api.dto.response

import com.raynor.demo.productservice.service.model.Product
import java.math.BigDecimal

data class ProductResponseDto(
    val id: Long,
    val name: String,
    val price: BigDecimal,
) {
    companion object
}

fun Product.toResponseDto() = ProductResponseDto(
    id = id.value,
    name = name.value,
    price = price.value,
)
