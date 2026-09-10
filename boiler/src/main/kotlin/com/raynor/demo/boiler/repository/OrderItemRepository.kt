package com.raynor.demo.boiler.repository

import com.raynor.demo.boiler.domain.order.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository : JpaRepository<OrderItem, Long>
