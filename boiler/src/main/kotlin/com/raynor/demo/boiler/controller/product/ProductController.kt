package com.raynor.demo.boiler.controller.product

import com.raynor.demo.boiler.controller.product.dto.ProductResponseDto
import com.raynor.demo.boiler.controller.support.CursorPageResponseDto
import com.raynor.demo.boiler.service.product.ProductService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {
    @GetMapping("")
    fun getProducts(
        @Min(0) @Max(100)
        @RequestParam("size", required = false, defaultValue = "20") size: Long,
        @RequestParam("cursor", required = false) cursor: Int?,
    ): ResponseEntity<CursorPageResponseDto<Int, ProductResponseDto>> {
        return productService.getProducts(size, cursor).let { cursorSlice ->
            ResponseEntity.ok(
                CursorPageResponseDto(
                    hasNext = cursorSlice.hasNext,
                    nextCursor = cursorSlice.nextCursor,
                    items = cursorSlice.items.map { product -> ProductResponseDto.fromModel(product) },
                ),
            )
        }
    }

    @GetMapping("/{id}")
    fun getProduct(
        @PathVariable("id") productId: Int,
    ): ResponseEntity<ProductResponseDto> {
        return productService.getProduct(productId).let { product ->
            ResponseEntity.ok(ProductResponseDto.fromModel(product))
        }
    }
}
