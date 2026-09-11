package com.raynor.demo.boiler.controller.order

import com.raynor.demo.boiler.controller.order.dto.OrderResponseDto
import com.raynor.demo.boiler.controller.support.CursorPageResponseDto
import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.service.order.OrderService
import com.raynor.demo.boiler.service.order.model.OrderModel
import com.raynor.demo.boiler.service.support.CursorSlice
import com.raynor.demo.boiler.support.ControllerTestContext
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

class OrderControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val orderService: OrderService,
) : ControllerTestContext(
        {
            val responseType = object : TypeReference<CursorPageResponseDto<Long, OrderResponseDto>>() {}

            test("GET /api/v1/orders - 유저의 주문 커서 페이지를 반환한다") {
                every {
                    orderService.getUserOrders(20, null, 1, null)
                } returns
                    CursorSlice(
                        hasNext = true,
                        nextCursor = 2L,
                        items =
                            listOf(
                                OrderModel(id = 3L),
                                OrderModel(id = 2L),
                            ),
                    )

                val content =
                    mockMvc
                        .get("/api/v1/orders") {
                            header("X-User-Id", "1")
                        }.andExpect { status { isOk() } }
                        .andReturn()
                        .response
                        .getContentAsString(Charsets.UTF_8)

                objectMapper.readValue(content, responseType) shouldBe
                    CursorPageResponseDto(
                        hasNext = true,
                        nextCursor = 2L,
                        items =
                            listOf(
                                OrderResponseDto(id = 3L),
                                OrderResponseDto(id = 2L),
                            ),
                    )

                verify(exactly = 1) { orderService.getUserOrders(20, null, 1, null) }
            }

            test("GET /api/v1/orders - size/cursor/orderStatus 파라미터와 X-User-Id 헤더를 서비스로 전달한다") {
                every {
                    orderService.getUserOrders(2, 5L, 7, listOf(OrderStatus.PAID, OrderStatus.CANCELED))
                } returns CursorSlice(hasNext = false, nextCursor = null, items = emptyList())

                val content =
                    mockMvc
                        .get("/api/v1/orders") {
                            header("X-User-Id", "7")
                            param("size", "2")
                            param("cursor", "5")
                            param("orderStatus", "PAID", "CANCELED")
                        }.andExpect { status { isOk() } }
                        .andReturn()
                        .response
                        .getContentAsString(Charsets.UTF_8)

                objectMapper.readValue(content, responseType) shouldBe
                    CursorPageResponseDto(
                        hasNext = false,
                        nextCursor = null,
                        items = emptyList<OrderResponseDto>(),
                    )

                verify(exactly = 1) {
                    orderService.getUserOrders(2, 5L, 7, listOf(OrderStatus.PAID, OrderStatus.CANCELED))
                }
            }

            test("GET /api/v1/orders - X-User-Id 헤더가 없으면 400 을 반환한다") {
                mockMvc
                    .get("/api/v1/orders")
                    .andExpect {
                        status { isBadRequest() }
                        jsonPath("$.status") { value(400) }
                    }

                verify(exactly = 0) { orderService.getUserOrders(any(), any(), any(), any()) }
            }

            test("GET /api/v1/orders - size 가 허용 범위를 벗어나면 400 을 반환한다") {
                mockMvc
                    .get("/api/v1/orders") {
                        header("X-User-Id", "1")
                        param("size", "101")
                    }.andExpect {
                        status { isBadRequest() }
                        jsonPath("$.status") { value(400) }
                        jsonPath("$.details[0].field") { value("size") }
                    }

                verify(exactly = 0) { orderService.getUserOrders(any(), any(), any(), any()) }
            }
        },
    )
