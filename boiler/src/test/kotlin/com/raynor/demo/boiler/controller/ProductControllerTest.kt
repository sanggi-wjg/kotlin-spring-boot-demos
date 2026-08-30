package com.raynor.demo.boiler.controller

import com.raynor.demo.boiler.controller.product.dto.ProductResponseDto
import com.raynor.demo.boiler.controller.support.CursorPageResponseDto
import com.raynor.demo.boiler.service.product.ProductService
import com.raynor.demo.boiler.service.product.model.ProductModel
import com.raynor.demo.boiler.service.support.CursorSlice
import com.raynor.demo.boiler.support.ControllerTestContext
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal

class ProductControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val productService: ProductService,
) : ControllerTestContext(
        {
            val responseType = object : TypeReference<CursorPageResponseDto<Int, ProductResponseDto>>() {}

            test("GET /api/v1/products - 커서 페이지를 반환한다") {
                every {
                    productService.getProducts(10L, null)
                } returns
                    CursorSlice(
                        hasNext = true,
                        nextCursor = 2,
                        items =
                            listOf(
                                ProductModel(1, "상품A", BigDecimal("1000.00"), 10L),
                                ProductModel(2, "상품B", BigDecimal("2000.00"), 20L),
                            ),
                    )

                val content =
                    mockMvc
                        .get("/api/v1/products")
                        .andExpect { status { isOk() } }
                        .andReturn()
                        .response
                        .getContentAsString(Charsets.UTF_8)

                objectMapper.readValue(content, responseType) shouldBe
                    CursorPageResponseDto(
                        hasNext = true,
                        nextCursor = 2,
                        items =
                            listOf(
                                ProductResponseDto(1, "상품A", BigDecimal("1000.00"), 10L),
                                ProductResponseDto(2, "상품B", BigDecimal("2000.00"), 20L),
                            ),
                    )

                verify(exactly = 1) { productService.getProducts(10L, null) }
            }

            test("GET /api/v1/products - perPage/cursor 쿼리 파라미터를 서비스로 전달한다") {
                every {
                    productService.getProducts(2L, 5)
                } returns CursorSlice(hasNext = false, nextCursor = null, items = emptyList())

                val content =
                    mockMvc
                        .get("/api/v1/products") {
                            param("perPage", "2")
                            param("cursor", "5")
                        }.andExpect { status { isOk() } }
                        .andReturn()
                        .response
                        .getContentAsString(Charsets.UTF_8)

                objectMapper.readValue(content, responseType) shouldBe
                    CursorPageResponseDto(
                        hasNext = false,
                        nextCursor = null,
                        items = emptyList<ProductResponseDto>(),
                    )

                verify(exactly = 1) { productService.getProducts(2L, 5) }
            }
        },
    )
