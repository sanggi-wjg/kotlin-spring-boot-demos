package com.raynor.demo.orderservice.rds.repository

import com.raynor.demo.orderservice.rds.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderEntity, Long>, OrderQueryDslRepository

interface OrderQueryDslRepository

class OrderQueryDslRepositoryImpl : OrderQueryDslRepository