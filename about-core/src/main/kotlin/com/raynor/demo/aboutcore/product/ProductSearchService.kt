package com.raynor.demo.aboutcore.product

import org.springframework.stereotype.Service


@Service
class ProductSearchService(
    private val productRepository: ProductRepository,
) {

    fun getProducts(): List<String> {
        return productRepository.findAll()
    }

    fun getSomething(): String {
        return productRepository.findById(1)
            ?: throw IllegalArgumentException("Product not found")
    }
}