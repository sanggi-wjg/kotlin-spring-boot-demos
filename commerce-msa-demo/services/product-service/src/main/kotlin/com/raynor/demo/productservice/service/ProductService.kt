package com.raynor.demo.productservice.service

import com.raynor.demo.productservice.rds.repository.ProductRdsRepository
import com.raynor.demo.productservice.service.model.Product
import com.raynor.demo.productservice.service.model.command.CreateProductCommand
import com.raynor.demo.productservice.service.model.query.ProductSearchQuery
import com.raynor.demo.productservice.service.model.toModel
import com.raynor.demo.shared.typed.product.ProductId
import com.raynor.demo.shared.typed.product.toProductId
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class ProductService(
    private val productRdsRepository: ProductRdsRepository
) {
    @Transactional
    fun createProduct(command: CreateProductCommand): ProductId {
        return productRdsRepository.save(command.toEntity()).let { product ->
            product.id!!.toProductId()
        }
    }

    @Transactional(readOnly = true)
    fun getProducts(searchQuery: ProductSearchQuery): List<Product> {
        return productRdsRepository.findPageByQuery(searchQuery).map { product ->
            product.toModel()
        }
    }

    @Transactional(readOnly = true)
    fun getProductById(id: ProductId): Product {
        return productRdsRepository.findByIdOrNull(id.value)?.toModel()
            ?: throw EntityNotFoundException("상품 ID: ${id}를 찾을 수 없습니다.")
    }
}
