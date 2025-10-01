package com.raynor.demo.productservice.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.productservice.RestControllerTestContext
import com.raynor.demo.productservice.api.dto.response.ProductResponseDto
import com.raynor.demo.productservice.service.ProductService
import com.raynor.demo.shared.typed.Money
import com.raynor.demo.shared.typed.product.ProductId
import com.raynor.demo.shared.typed.product.ProductName
import io.mockk.every
import org.springframework.test.web.servlet.MockMvc
import java.math.BigDecimal

class ProductRestControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val productService: ProductService,
) : RestControllerTestContext({

    context("상품 조회 API") {

        test("GET /api/v1/products/{id}") {
            val productId = ProductId(1L)

            // mock
            every {
                productService.getProductById(productId)
            } returns ProductResponseDto(
                id = productId,
                name = ProductName("상품"),
                price = Money(BigDecimal("1000")),
            )
        }
    }
})