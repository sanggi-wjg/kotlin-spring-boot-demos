package com.raynor.demo.miniioc.example

interface OrderService

class DefaultOrderService(
    val orderRepository: OrderRepository,
) : OrderService
