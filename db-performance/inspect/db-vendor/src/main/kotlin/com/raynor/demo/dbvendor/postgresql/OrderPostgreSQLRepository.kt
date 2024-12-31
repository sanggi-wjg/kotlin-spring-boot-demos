package com.raynor.demo.dbvendor.postgresql

import com.raynor.demo.dbvendor.enum.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderPostgreSQLRepository : JpaRepository<OrderPostgreSQLEntity, Int> {
    fun findAllByStatus(status: OrderStatus): List<OrderPostgreSQLEntity>
}
