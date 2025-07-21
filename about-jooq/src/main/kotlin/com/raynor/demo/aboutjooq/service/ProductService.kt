package com.raynor.demo.aboutjooq.service

import com.raynor.demo.aboutjooq.model.ProductModel
import com.raynor.demo.aboutjooq.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun getAllProducts(): List<ProductModel> {
        return productRepository.findAll().map {
            ProductModel.from(it)
        }
    }

    @Transactional(readOnly = true)
    fun getProductById(id: Int): ProductModel {
        return productRepository.findById(id)?.let {
            ProductModel.from(it)
        } ?: throw RuntimeException("Product not found")
    }
}