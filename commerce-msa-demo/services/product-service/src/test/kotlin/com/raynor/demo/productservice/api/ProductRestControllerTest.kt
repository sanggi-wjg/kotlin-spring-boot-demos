package com.raynor.demo.productservice.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.productservice.RestControllerTestContext
import com.raynor.demo.productservice.api.dto.response.ProductResponseDto
import com.raynor.demo.productservice.service.ProductService
import com.raynor.demo.productservice.service.model.Product
import com.raynor.demo.shared.typed.Money
import com.raynor.demo.shared.typed.product.ProductId
import com.raynor.demo.shared.typed.product.ProductName
import com.raynor.demo.shared.typed.product.toProductStockQuantity
import io.mockk.every
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

class ProductRestControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val productService: ProductService,
) : RestControllerTestContext({

    context("상품 조회 API") {

        test("GET /api/v1/products/{id}") {
            val productResponseDto = ProductResponseDto(
                id = ProductId(123L),
                name = ProductName("상품"),
                price = Money(BigDecimal("1000")),
            )

            // mock
            every {
                productService.getProductById(productResponseDto.id)
            } returns Product(
                id = productResponseDto.id,
                name = productResponseDto.name,
                price = productResponseDto.price,
                stockQuantity = 10.toProductStockQuantity(),
            )

            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/products/{id}", productResponseDto.id.value)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(productResponseDto.id.value))
                .andExpect(jsonPath("$.name").value(productResponseDto.name.value))
                .andExpect(jsonPath("$.price").value(productResponseDto.price.value))
        }
    }
})
