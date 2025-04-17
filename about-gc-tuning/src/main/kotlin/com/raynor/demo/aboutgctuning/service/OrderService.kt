package com.raynor.demo.aboutgctuning.service

import com.raynor.demo.aboutgctuning.entity.OrderEntity
import com.raynor.demo.aboutgctuning.model.Order
import com.raynor.demo.aboutgctuning.repository.OrderRepository
import com.raynor.demo.aboutgctuning.repository.ProductRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
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
    fun getOrders(pageRequest: PageRequest): List<Order> {
        return orderRepository.findAll(pageRequest).let { paged ->
            paged.content.map { Order.valueOf(it) }
        }
    }

    @Transactional(readOnly = true)
    fun getOrdersRealTime(pageRequest: PageRequest): List<Order> {
        return orderRepository.findAll(pageRequest).let { paged ->
            paged.content.map { Order.valueOf(it) }
        }
    }

    @Cacheable(value = ["orders"], key = "#orderId")
    @Transactional(readOnly = true)
    fun getOrder(orderId: Int): Order {
        val order = orderRepository.findByIdOrNull(orderId)
            ?: throw EntityNotFoundException("Order not found with id:$orderId")
        return Order.valueOf(order)
    }

    @CachePut(value = ["orders"], key = "#orderId")
    @Transactional
    fun completeOrder(orderId: Int): Order {
        val order = orderRepository.findByIdOrNull(orderId)
            ?: throw EntityNotFoundException("Order not found with id:$orderId")
        order.complete()
        return Order.valueOf(order)
    }
}
