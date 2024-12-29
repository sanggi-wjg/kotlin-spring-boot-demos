package com.raynor.demo.dbvendor.service

import com.raynor.demo.dbvendor.postgresql.OrderPostgreSQLEntity
import com.raynor.demo.dbvendor.postgresql.OrderPostgreSQLRepository
import com.raynor.demo.dbvendor.postgresql.PostgreSQLConfig
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Service(PostgreSQLPerformanceService.BEAN_NAME)
@Transactional(PostgreSQLConfig.TRANSACTION_MANAGER)
class PostgreSQLPerformanceService(
    private val orderPostgreSQLRepository: OrderPostgreSQLRepository,
) : PerformanceService {

    companion object {
        const val BEAN_NAME = "postgreSQLPerformanceService"
    }

    override fun insert() {
        val now = Instant.now()
        val person = OrderPostgreSQLEntity(
            orderNumber = "123",
            amount = BigDecimal(1000),
            createdAt = now,
        )
        orderPostgreSQLRepository.save(person)
    }
}