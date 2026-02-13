package com.raynor.demo.orderservice.api

import com.raynor.demo.orderservice.api.dto.request.CreateOrderRequestDto
import com.raynor.demo.orderservice.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class OrderRestController(
    private val orderService: OrderService,
) {
    @PostMapping(
        "",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createOrder(
        @Valid @RequestBody requestDto: CreateOrderRequestDto,
    ): ResponseEntity<String> {
        return orderService.createOrder(requestDto.toCommand()).let {
            ResponseEntity.ok("Order created")
        }
    }
}