package com.raynor.demo.aboutgctuning.controller

import com.raynor.demo.aboutgctuning.entity.OrderEntity
import com.raynor.demo.aboutgctuning.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @PostMapping("/")
    fun createOrder(): ResponseEntity<OrderEntity> {
        return orderService.createOrder().let {
            ResponseEntity.created(URI.create("/orders/${it.id}")).body(it)
        }
    }
}