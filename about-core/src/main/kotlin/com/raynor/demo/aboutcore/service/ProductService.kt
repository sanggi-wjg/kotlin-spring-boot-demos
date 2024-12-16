package com.raynor.demo.aboutcore.service

import org.springframework.stereotype.Service


@Service
class ProductService {

    fun getProducts(): List<String> {
        return listOf("product 1", "product 2", "product 3")
    }

    fun getSomething(): String {
        return "something"
    }

    fun findProducts(): List<String> {
        return listOf("product 1", "product 2", "product 3")
    }
}