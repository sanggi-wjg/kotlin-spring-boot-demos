package com.raynor.demo.boiler.service.product

import com.raynor.demo.boiler.repository.ProductRepository
import com.raynor.demo.boiler.service.product.model.ProductModel
import com.raynor.demo.boiler.service.support.CursorSlice
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun getProducts(
        size: Long,
        cursor: Int?,
    ): CursorSlice<Int, ProductModel> {
        val products = productRepository.findAllByCursor(size + 1, cursor)
        val items = products.take(size.toInt()).map { ProductModel.fromEntity(it) }
        return CursorSlice(
            hasNext = products.size > size,
            nextCursor = items.lastOrNull()?.id,
            items = items,
        )
    }

    @Transactional(readOnly = true)
    fun getProduct(productId: Int): ProductModel {
        val product = productRepository.findByIdOrNull(productId)
            ?: throw EntityNotFoundException("")
        return ProductModel.fromEntity(product)
    }
}
