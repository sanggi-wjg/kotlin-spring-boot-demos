package com.raynor.demo.aboutgctuning.controller

import com.raynor.demo.aboutgctuning.model.Order
import com.raynor.demo.aboutgctuning.service.OrderService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import kotlin.random.Random

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
        val pageRequest = PageRequest.of(
            Random.nextInt(0, 100),
            Random.nextInt(10, 100),
        )
        return orderService.getOrders(pageRequest).let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/realtime")
    fun getOrdersRealTime(): ResponseEntity<List<Order>> {
        val pageRequest = PageRequest.of(
            Random.nextInt(0, 100),
            Random.nextInt(10, 100),
        )
        return orderService.getOrdersRealTime(pageRequest).let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Int): ResponseEntity<Order> {
        return orderService.getOrder(orderId).let {
            ResponseEntity.ok(it)
        }
    }

    @PostMapping("/{orderId}/complete")
    fun completeOrder(@PathVariable orderId: Int): ResponseEntity<Order> {
        return orderService.completeOrder(orderId).let {
            ResponseEntity.ok(it)
        }
    }
}