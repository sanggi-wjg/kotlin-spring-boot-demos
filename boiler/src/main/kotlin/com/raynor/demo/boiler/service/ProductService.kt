package com.raynor.demo.boiler.service

import com.raynor.demo.boiler.domain.product.Product
import com.raynor.demo.boiler.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun getProducts(): List<Product> = productRepository.findAll()
}
