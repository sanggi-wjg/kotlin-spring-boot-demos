package com.raynor.demo.boiler.service.product

import com.raynor.demo.boiler.repository.ProductRepository
import com.raynor.demo.boiler.service.product.model.ProductModel
import com.raynor.demo.boiler.service.support.CursorSlice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun getProducts(
        perPage: Long,
        cursor: Int?,
    ): CursorSlice<Int, ProductModel> {
        val products = productRepository.findAllByCursor(perPage + 1, cursor)
        val items = products.take(perPage.toInt()).map { ProductModel.fromEntity(it) }
        return CursorSlice(
            hasNext = products.size > perPage,
            nextCursor = items.lastOrNull()?.id,
            items = items,
        )
    }
}
