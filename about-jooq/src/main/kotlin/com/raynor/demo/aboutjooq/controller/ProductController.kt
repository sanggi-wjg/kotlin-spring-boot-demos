package com.raynor.demo.aboutjooq.controller

import com.raynor.demo.aboutjooq.model.ProductModel
import com.raynor.demo.aboutjooq.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class ProductController(
    private val productService: ProductService,
) {

    @GetMapping("/products")
    fun getAllProducts(): ResponseEntity<List<ProductModel>> {
        return productService.getAllProducts().let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/products/{productId}")
    fun getProductById(@PathVariable productId: Int): ResponseEntity<ProductModel> {
        return productService.getProductById(productId).let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/products/create-1")
    fun createProduct1(): ResponseEntity<String> {
        return productService.createProducts1().let {
            ResponseEntity.ok("123")
        }
    }

    @GetMapping("/products/create-2")
    fun createProduct2(): ResponseEntity<String> {
        return productService.createProducts2().let {
            ResponseEntity.ok("123")
        }
    }
}