package com.raynor.demo.aboutgctuning.controller

import com.raynor.demo.aboutgctuning.model.Order
import com.raynor.demo.aboutgctuning.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @PostMapping("")
    fun createOrder(): ResponseEntity<Order> {
        return orderService.createOrder().let {
            ResponseEntity.created(URI.create("/orders/${it.id}")).body(it)
        }
    }

    @GetMapping("")
    fun getOrders(): ResponseEntity<List<Order>> {
        return orderService.getOrders().let {
            ResponseEntity.ok(it)
        }
    }
}