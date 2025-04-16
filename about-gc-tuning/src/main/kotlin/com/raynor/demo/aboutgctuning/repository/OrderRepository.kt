package com.raynor.demo.aboutgctuning.repository

import com.raynor.demo.aboutgctuning.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderEntity, Int>
