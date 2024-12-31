package com.raynor.demo.dbvendor.mysql

import com.raynor.demo.dbvendor.enum.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderMySQLRepository : JpaRepository<OrderMySQLEntity, Int> {
    fun findAllByStatus(status: OrderStatus): List<OrderMySQLEntity>
}
