package com.raynor.demo.boiler.controller.order

import com.raynor.demo.boiler.controller.order.dto.CreateOrderRequestDto
import com.raynor.demo.boiler.controller.order.dto.OrderResponseDto
import com.raynor.demo.boiler.controller.support.CursorPageResponseDto
import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.service.order.OrderService
import com.raynor.demo.boiler.shared.http.ApiHeaders
import com.raynor.demo.boiler.shared.idempotency.Idempotent
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @GetMapping("")
    fun getOrders(
        @Min(1) @Max(100)
        @RequestParam("size", required = false, defaultValue = "20") size: Int,
        @RequestParam("cursor", required = false) cursor: Long?,
        @RequestParam("orderStatus", required = false) orderStatus: List<OrderStatus>?,
        @RequestHeader(ApiHeaders.USER_ID) userId: Int,
    ): ResponseEntity<CursorPageResponseDto<Long, OrderResponseDto>> {
        return orderService.getUserOrders(size, cursor, userId, orderStatus).let { cursorSlice ->
            ResponseEntity.ok(
                CursorPageResponseDto(
                    hasNext = cursorSlice.hasNext,
                    nextCursor = cursorSlice.nextCursor,
                    items = cursorSlice.items.map { item -> OrderResponseDto.fromModel(item) },
                ),
            )
        }
    }

    @Idempotent
    @PostMapping("")
    fun createOrder(
        @RequestHeader(ApiHeaders.IDEMPOTENCY_KEY) idempotencyKey: String,
        @RequestHeader(ApiHeaders.USER_ID) userId: Int,
        @RequestBody request: CreateOrderRequestDto,
    ): ResponseEntity<String> {
        return ResponseEntity.ok("주문 생성 완료")
    }
}
