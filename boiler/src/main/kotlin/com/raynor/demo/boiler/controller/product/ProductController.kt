package com.raynor.demo.boiler.controller.product

import com.raynor.demo.boiler.controller.product.dto.ProductResponseDto
import com.raynor.demo.boiler.controller.support.CursorPageResponseDto
import com.raynor.demo.boiler.service.product.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
        @RequestParam("perPage", required = false, defaultValue = "10") perPage: Long,
        @RequestParam("cursor", required = false) cursor: Int?,
    ): ResponseEntity<CursorPageResponseDto<Int, ProductResponseDto>> {
        return productService.getProducts(perPage, cursor).let { cursorSlice ->
            ResponseEntity.ok(
                CursorPageResponseDto(
                    hasNext = cursorSlice.hasNext,
                    nextCursor = cursorSlice.nextCursor,
                    items = cursorSlice.items.map { product -> ProductResponseDto.fromModel(product) },
                ),
            )
        }
    }
}
