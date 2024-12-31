package com.raynor.demo.dbvendor.service

import com.raynor.demo.dbvendor.enum.OrderStatus
import com.raynor.demo.dbvendor.mongodb.OrderDocument
import com.raynor.demo.dbvendor.mongodb.OrderMongoDBRepository
import com.raynor.demo.support.annotation.Benchmark
import com.raynor.demo.support.config.Constants
import com.raynor.demo.support.config.Constants.CYCLE_COUNT
import org.bson.types.ObjectId
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
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

    @Benchmark
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
        repeat(CYCLE_COUNT / 5) {
            val rand = Random.nextInt(0, Constants.CYCLE_COUNT)
            val page = PageRequest.of(rand, Constants.OFFSET)
            orderRepository.findAll(page)
        }
    }

    @Benchmark
    override fun readByPk() {
        repeat(CYCLE_COUNT / 5) {
            orderRepository.findByIdOrNull(ObjectId("6773e7225b0a067e375e67a8"))
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