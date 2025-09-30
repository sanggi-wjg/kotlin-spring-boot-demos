package com.raynor.demo.productservice.api

import com.raynor.demo.productservice.api.dto.request.CreateProductRequestDto
import com.raynor.demo.productservice.api.dto.response.ProductIdResponseDto
import com.raynor.demo.productservice.api.dto.response.ProductResponseDto
import com.raynor.demo.productservice.service.ProductService
import com.raynor.demo.productservice.service.condition.ProductSearchCondition
import com.raynor.demo.productservice.service.condition.ProductSortBy
import com.raynor.demo.productservice.service.condition.SortDirection
import com.raynor.demo.shared.typed.product.toProductId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/v1/products")
class ProductRestController(
    private val productService: ProductService
) {
    @PostMapping("")
    fun createProduct(
        @RequestBody requestDto: CreateProductRequestDto
    ): ResponseEntity<ProductIdResponseDto> {
        return productService.createProduct(requestDto).let {
            ResponseEntity.created(URI.create("/api/v1/products/$it"))
                .body(ProductIdResponseDto(it))
        }
    }

    @GetMapping("")
    fun getProducts(
        @RequestParam(defaultValue = "5") size: Long,
        @RequestParam(required = false) lastId: Long?,
        @RequestParam(defaultValue = "ID") sortBy: ProductSortBy,
        @RequestParam(defaultValue = "DESC") sortDirection: SortDirection
    ): ResponseEntity<List<ProductResponseDto>> {
        return productService.getProducts(
            ProductSearchCondition(
                size = size,
                sortBy = sortBy,
                sortDirection = sortDirection,
                lastId = lastId?.toProductId(),
            )
        ).let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/{id}")
    fun getProductById(
        @PathVariable id: Long
    ): ResponseEntity<ProductResponseDto> {
        return productService.getProductById(id).let {
            ResponseEntity.ok(it)
        }
    }
}
