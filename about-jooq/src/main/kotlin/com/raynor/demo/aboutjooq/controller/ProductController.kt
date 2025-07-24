package com.raynor.demo.aboutjooq.controller

import com.raynor.demo.aboutjooq.model.ProductModel
import com.raynor.demo.aboutjooq.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

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

    @PutMapping("/products/{productId}")
    fun updateProduct(@PathVariable productId: Int): ResponseEntity<Unit> {
        return productService.update(productId).let {
            ResponseEntity.noContent().build()
        }
    }

    @DeleteMapping("/products/{productId}")
    fun deleteProduct(@PathVariable productId: Int): ResponseEntity<Unit> {
        return productService.delete(productId).let {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping("/products/create-1")
    fun createProduct1(): ResponseEntity<String> {
        return productService.createProducts1().let {
            ResponseEntity.created(URI.create("/api/v1/products/123")).build()
        }
    }

    @GetMapping("/products/create-2")
    fun createProduct2(): ResponseEntity<String> {
        return productService.createProducts2().let {
            ResponseEntity.created(URI.create("/api/v1/products/123")).build()
        }
    }

    @GetMapping("/products/create-3")
    fun createProduct3(): ResponseEntity<List<ProductModel>> {
        return productService.createProducts3().let {
            ResponseEntity.created(URI.create("/api/v1/products/123")).build()
        }
    }
}