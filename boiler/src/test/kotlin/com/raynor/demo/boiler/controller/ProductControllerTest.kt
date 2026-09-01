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
                    productService.getProducts(20, null)
                } returns
                    CursorSlice(
                        hasNext = true,
                        nextCursor = 2,
                        items =
                            listOf(
                                ProductModel(
                                    id = 1,
                                    name = "상품A",
                                    price = BigDecimal("1000.00"),
                                    stockQuantity = 10L,
                                    status = "ON_SALE",
                                    isSoldOut = false,
                                    isSale = true,
                                ),
                                ProductModel(
                                    id = 2,
                                    name = "상품B",
                                    price = BigDecimal("2000.00"),
                                    stockQuantity = 0L,
                                    status = "ON_SALE",
                                    isSoldOut = true,
                                    isSale = false,
                                ),
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
                                ProductResponseDto(
                                    id = 1,
                                    name = "상품A",
                                    price = BigDecimal("1000.00"),
                                    stockQuantity = 10L,
                                    status = "ON_SALE",
                                    isSoldOut = false,
                                    isSale = true,
                                ),
                                ProductResponseDto(
                                    id = 2,
                                    name = "상품B",
                                    price = BigDecimal("2000.00"),
                                    stockQuantity = 0L,
                                    status = "ON_SALE",
                                    isSoldOut = true,
                                    isSale = false,
                                ),
                            ),
                    )

                verify(exactly = 1) { productService.getProducts(20, null) }
            }

            test("GET /api/v1/products - size/cursor 쿼리 파라미터를 서비스로 전달한다") {
                every {
                    productService.getProducts(2, 5)
                } returns CursorSlice(hasNext = false, nextCursor = null, items = emptyList())

                val content =
                    mockMvc
                        .get("/api/v1/products") {
                            param("size", "2")
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

                verify(exactly = 1) { productService.getProducts(2, 5) }
            }
        },
    )
