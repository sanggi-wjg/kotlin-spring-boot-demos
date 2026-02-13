package com.raynor.demo.orderservice.rds.repository

import com.raynor.demo.orderservice.rds.entity.OrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository : JpaRepository<OrderItemEntity, Long>, OrderItemQueryDslRepository

interface OrderItemQueryDslRepository

class OrderItemQueryDslRepositoryImpl : OrderItemQueryDslRepository