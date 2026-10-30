package com.raynor.demo.boiler.controller.order

import com.raynor.demo.boiler.controller.order.dto.CreateOrderRequestDto
import com.raynor.demo.boiler.controller.order.dto.OrderResponseDto
import com.raynor.demo.boiler.controller.support.CursorPageResponseDto
import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.service.order.OrderFacadeService
import com.raynor.demo.boiler.service.order.OrderService
import com.raynor.demo.boiler.service.order.model.OrderModel
import com.raynor.demo.boiler.service.support.CursorSlice
import com.raynor.demo.boiler.shared.http.ApiHeaders
import com.raynor.demo.boiler.shared.idempotency.IdempotencyKeyReusedException
import com.raynor.demo.boiler.shared.idempotency.IdempotencyRequestInProgressException
import com.raynor.demo.boiler.support.ControllerTestContext
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class OrderControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val orderService: OrderService,
    private val orderFacadeService: OrderFacadeService,
) : ControllerTestContext(
        {
            val cursorPageResponseType = object : TypeReference<CursorPageResponseDto<Long, OrderResponseDto>>() {}

            context("GET /api/v1/orders") {
                test("유저의 주문 커서 페이지를 반환한다") {
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

                    val content = mockMvc.get("/api/v1/orders") {
                        header(ApiHeaders.USER_ID, "1")
                    }.andExpect { status { isOk() } }
                        .andReturn()
                        .response
                        .getContentAsString(Charsets.UTF_8)

                    objectMapper.readValue(content, cursorPageResponseType) shouldBe
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

                test("size/cursor/orderStatus 파라미터와 X-User-Id 헤더를 서비스로 전달한다") {
                    every {
                        orderService.getUserOrders(2, 5L, 7, listOf(OrderStatus.PAID, OrderStatus.CANCELED))
                    } returns CursorSlice(hasNext = false, nextCursor = null, items = emptyList())

                    val content = mockMvc
                        .get("/api/v1/orders") {
                            header(ApiHeaders.USER_ID, "7")
                            param("size", "2")
                            param("cursor", "5")
                            param("orderStatus", "PAID", "CANCELED")
                        }.andExpect { status { isOk() } }
                        .andReturn()
                        .response
                        .getContentAsString(Charsets.UTF_8)

                    objectMapper.readValue(content, cursorPageResponseType) shouldBe
                        CursorPageResponseDto(
                            hasNext = false,
                            nextCursor = null,
                            items = emptyList<OrderResponseDto>(),
                        )

                    verify(exactly = 1) {
                        orderService.getUserOrders(2, 5L, 7, listOf(OrderStatus.PAID, OrderStatus.CANCELED))
                    }
                }

                test("X-User-Id 헤더가 없으면 400 을 반환한다") {
                    mockMvc.get("/api/v1/orders")
                        .andExpect {
                            status { isBadRequest() }
                            jsonPath("$.status") { value(400) }
                        }

                    verify(exactly = 0) { orderService.getUserOrders(any(), any(), any(), any()) }
                }

                test("size 가 허용 범위를 벗어나면 400 을 반환한다") {
                    mockMvc.get("/api/v1/orders") {
                        header(ApiHeaders.USER_ID, "1")
                        param("size", "101")
                    }.andExpect {
                        status { isBadRequest() }
                        jsonPath("$.status") { value(400) }
                        jsonPath("$.details[0].field") { value("size") }
                    }

                    verify(exactly = 0) { orderService.getUserOrders(any(), any(), any(), any()) }
                }
            }

            context("POST /api/v1/orders") {

                test("유저 주문 생성") {
                    // given
                    val requestDto = CreateOrderRequestDto(
                        items = listOf(CreateOrderRequestDto.Item(productId = 1, quantity = 1)),
                        couponId = null,
                    )

                    // mock
                    every {
                        orderFacadeService.createOrder(any(), any())
                    } returns OrderModel(id = 1)

                    // when
                    mockMvc.post("/api/v1/orders") {
                        header(ApiHeaders.USER_ID, "1")
                        header(ApiHeaders.IDEMPOTENCY_KEY, UUID.randomUUID().toString())
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(requestDto)
                    }.andExpect {
                        status { isCreated() }
                    }

                    // then
                }

                test("IdempotencyRequestInProgressException 은 409 와 Retry-After 를 ErrorResponseDto 형식으로 변환한다") {
                    // given
                    val requestDto = CreateOrderRequestDto(
                        items = listOf(CreateOrderRequestDto.Item(productId = 1, quantity = 1)),
                        couponId = null,
                    )

                    // mock
                    every {
                        orderFacadeService.createOrder(any(), any())
                    } throws IdempotencyRequestInProgressException("동일한 요청을 처리하고 있습니다.")

                    // when, then
                    mockMvc.post("/api/v1/orders") {
                        header(ApiHeaders.USER_ID, "1")
                        header(ApiHeaders.IDEMPOTENCY_KEY, UUID.randomUUID().toString())
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(requestDto)
                    }.andExpect {
                        status { isConflict() }
                        header { string(HttpHeaders.RETRY_AFTER, "1") }
                        content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                        jsonPath("$.status") { value(409) }
                        jsonPath("$.message") { value("동일한 요청을 처리하고 있습니다.") }
                    }
                }

                test("IdempotencyKeyReusedException 은 422 를 ErrorResponseDto 형식으로 변환한다") {
                    // given
                    val requestDto = CreateOrderRequestDto(
                        items = listOf(CreateOrderRequestDto.Item(productId = 1, quantity = 1)),
                        couponId = null,
                    )

                    // mock
                    every {
                        orderFacadeService.createOrder(any(), any())
                    } throws IdempotencyKeyReusedException("Idempotency-Key 가 다른 요청 본문으로 이미 사용되었습니다.")

                    // when, then
                    mockMvc.post("/api/v1/orders") {
                        header(ApiHeaders.USER_ID, "1")
                        header(ApiHeaders.IDEMPOTENCY_KEY, UUID.randomUUID().toString())
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(requestDto)
                    }.andExpect {
                        status { isUnprocessableContent() }
                        content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                        jsonPath("$.status") { value(422) }
                        jsonPath("$.message") { value("Idempotency-Key 가 다른 요청 본문으로 이미 사용되었습니다.") }
                    }
                }
            }
        },
    )
