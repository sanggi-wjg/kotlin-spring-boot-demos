package com.raynor.demo.aboutcore.product

import org.springframework.stereotype.Repository

@Repository
class ProductRepository {

    fun findAll(): List<String> {
        return listOf("product 1", "product 2", "product 3")
    }

    fun findById(id: Int): String? {
        return if (id == 1) {
            "product 1"
        } else {
            null
        }
    }
}