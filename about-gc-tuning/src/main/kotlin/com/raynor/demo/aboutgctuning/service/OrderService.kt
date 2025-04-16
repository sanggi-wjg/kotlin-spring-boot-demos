package com.raynor.demo.aboutgctuning.service

import com.raynor.demo.aboutgctuning.entity.OrderEntity
import com.raynor.demo.aboutgctuning.model.Order
import com.raynor.demo.aboutgctuning.repository.OrderRepository
import com.raynor.demo.aboutgctuning.repository.ProductRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.random.Random

@Service
class OrderService(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {

    @CacheEvict(value = ["orders"])
    @Transactional
    fun createOrder(): Order {
        val product = productRepository.findAll().random()
        val quantity = Random.nextInt(1, 10)
        val order = OrderEntity(
            orderNumber = Instant.now().toEpochMilli().toString(),
            amount = product.price * quantity.toBigDecimal(),
            quantity = quantity,
            createdAt = Instant.now(),
            product = product
        )
        return orderRepository.save(order).let {
            Order.valueOf(it)
        }
    }

    @Cacheable(value = ["orders"])
    @Transactional(readOnly = true)
    fun getOrders(): List<Order> {
        return orderRepository.findAll().map {
            Order.valueOf(it)
        }
    }
}
