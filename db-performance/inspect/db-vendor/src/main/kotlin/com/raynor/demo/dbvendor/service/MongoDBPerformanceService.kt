package com.raynor.demo.dbvendor.service

import com.raynor.demo.dbvendor.mongodb.OrderDocument
import com.raynor.demo.dbvendor.mongodb.OrderMongoDBRepository
import com.raynor.demo.support.config.Constants
import com.raynor.demo.support.config.Constants.CYCLE_COUNT
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import kotlin.random.Random

//@Transactional(MongoDBConfig.TRANSACTION_MANAGER) // mongo db는 replica 환경에서만 트랜잭션 사용 가능
@Service(MongoDBPerformanceService.BEAN_NAME)
class MongoDBPerformanceService(
    private val orderRepository: OrderMongoDBRepository,
) : PerformanceService {

    companion object {
        const val BEAN_NAME = "mongodbPerformanceService"
    }

    override fun create() {
        val now = Instant.now()
        val persons = List(Constants.INSERT_SIZE) {
            OrderDocument(
                orderNumber = "bulk-insert",
                amount = BigDecimal(2222),
                createdAt = now,
            )
        }
        orderRepository.saveAll(persons)
    }

    override fun update() {
        val now = Instant.now()
        val rand = Random.nextInt(0, Constants.SELECT_RANGE)
        val page = PageRequest.of(rand, Constants.OFFSET)
        orderRepository.findAll(page).forEach {
            it.complete(now)
        }
    }

    override fun read() {
        repeat(CYCLE_COUNT / 5) {
            val rand = Random.nextInt(0, Constants.CYCLE_COUNT)
            val page = PageRequest.of(rand, Constants.OFFSET)
            orderRepository.findAll(page)
        }
    }

    override fun delete() {
        val page = PageRequest.of(0, Constants.OFFSET)
        orderRepository.findAll(page).also {
            orderRepository.deleteAll(it)
        }
    }
}