package com.raynor.demo.dbvendor.service

import com.raynor.demo.dbvendor.enum.OrderStatus
import com.raynor.demo.dbvendor.mysql.MySQLConfig
import com.raynor.demo.dbvendor.mysql.OrderMySQLEntity
import com.raynor.demo.dbvendor.mysql.OrderMySQLRepository
import com.raynor.demo.support.annotation.Benchmark
import com.raynor.demo.support.config.Constants
import com.raynor.demo.support.config.Constants.CYCLE_COUNT
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import kotlin.random.Random

@Service(MySQLPerformanceService.BEAN_NAME)
@Transactional(MySQLConfig.TRANSACTION_MANAGER)
class MySQLPerformanceService(
    private val orderRepository: OrderMySQLRepository,
) : PerformanceService {

    companion object {
        const val BEAN_NAME = "mysqlPerformanceService"
    }

    @Benchmark
    override fun create() {
        val now = Instant.now()
        val persons = List(Constants.INSERT_SIZE) {
            OrderMySQLEntity(
                orderNumber = "bulk-insert",
                amount = BigDecimal(2222),
                createdAt = now,
            )
        }
        orderRepository.saveAll(persons)
    }

    @Benchmark
    override fun update() {
        val now = Instant.now()
        val rand = Random.nextInt(0, Constants.SELECT_RANGE)
        val page = PageRequest.of(rand, Constants.OFFSET)
        orderRepository.findAll(page).forEach {
            it.complete(now)
        }
    }

    @Benchmark
    override fun read() {
        repeat(Constants.CYCLE_COUNT / 5) {
            val rand = Random.nextInt(0, Constants.CYCLE_COUNT)
            val page = PageRequest.of(rand, Constants.OFFSET)
            orderRepository.findAll(page)
        }
    }

    @Benchmark
    override fun readByPk() {
        repeat(CYCLE_COUNT / 5) {
            orderRepository.findByIdOrNull(1)
        }
    }

    @Benchmark
    override fun readByNoneIndexColumn() {
        repeat(CYCLE_COUNT / 5) {
            orderRepository.findAllByStatus(OrderStatus.NEW_ORDER)
        }
    }

    @Benchmark
    override fun delete() {
        val page = PageRequest.of(0, Constants.OFFSET)
        orderRepository.findAll(page).also {
            orderRepository.deleteAll(it)
        }
    }
}