package com.raynor.demo.aboutgctuning.service

import com.raynor.demo.aboutgctuning.entity.OrderEntity
import com.raynor.demo.aboutgctuning.repository.OrderRepository
import com.raynor.demo.aboutgctuning.repository.ProductRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.random.Random

@Service
class OrderService(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {

    @Transactional
    fun createOrder(): OrderEntity {
        val product = productRepository.findAll().random()
        val quantity = Random.nextInt(1, 10)
        val order = OrderEntity(
            orderNumber = Instant.now().toEpochMilli().toString(),
            amount = product.price * quantity.toBigDecimal(),
            quantity = quantity,
            createdAt = Instant.now(),
            product = product
        )
        return orderRepository.save(order)
    }
}
