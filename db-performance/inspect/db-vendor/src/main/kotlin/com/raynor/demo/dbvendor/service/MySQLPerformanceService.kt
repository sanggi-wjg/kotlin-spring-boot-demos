package com.raynor.demo.dbvendor.service

import com.raynor.demo.dbvendor.mysql.MySQLConfig
import com.raynor.demo.dbvendor.mysql.OrderMySQLEntity
import com.raynor.demo.dbvendor.mysql.OrderMySQLRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Service(MySQLPerformanceService.BEAN_NAME)
@Transactional(MySQLConfig.TRANSACTION_MANAGER)
class MySQLPerformanceService(
    private val orderMySQLRepository: OrderMySQLRepository,
) : PerformanceService {

    companion object {
        const val BEAN_NAME = "mysqlPerformanceService"
    }

    override fun insert() {
        val now = Instant.now()
        val person = OrderMySQLEntity(
            orderNumber = "123",
            amount = BigDecimal(1000),
            createdAt = now,
        )
        orderMySQLRepository.save(person)
    }
}