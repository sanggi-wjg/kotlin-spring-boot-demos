package com.raynor.demo.miniioc.beans

interface OrderService

class DefaultOrderService(
    val orderRepository: OrderRepository,
) : OrderService
