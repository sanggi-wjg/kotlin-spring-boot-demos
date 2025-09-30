package com.raynor.demo.productservice.api.dto.response

import com.raynor.demo.shared.typed.Money
import com.raynor.demo.shared.typed.product.ProductId
import com.raynor.demo.shared.typed.product.ProductName

data class ProductResponseDto(
    val id: ProductId,
    val name: ProductName,
    val price: Money,
)