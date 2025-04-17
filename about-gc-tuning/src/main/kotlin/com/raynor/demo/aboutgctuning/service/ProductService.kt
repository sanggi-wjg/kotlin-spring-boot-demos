package com.raynor.demo.aboutgctuning.service

import com.raynor.demo.aboutgctuning.entity.ProductEntity
import com.raynor.demo.aboutgctuning.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {

    @Transactional(readOnly = true)
    fun getProducts(): List<ProductEntity> {
        return productRepository.findAll()
    }
}