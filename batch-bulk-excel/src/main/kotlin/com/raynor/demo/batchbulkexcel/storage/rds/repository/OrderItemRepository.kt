package com.raynor.demo.batchbulkexcel.storage.rds.repository

import com.raynor.demo.batchbulkexcel.storage.rds.entity.OrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository : JpaRepository<OrderItemEntity, Long>
