package com.raynor.demo.productservice.service.condition

import com.raynor.demo.shared.typed.product.ProductId

enum class ProductSortBy {
    ID,
}

enum class SortDirection {
    ASC, DESC,
}

data class ProductSearchCondition(
    val size: Long = 10,
    val sortBy: ProductSortBy = ProductSortBy.ID,
    val sortDirection: SortDirection = SortDirection.DESC,
    val lastId: ProductId? = null,
)