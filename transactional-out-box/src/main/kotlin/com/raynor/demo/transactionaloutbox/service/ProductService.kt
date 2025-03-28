package com.raynor.demo.transactionaloutbox.service

import com.raynor.demo.transactionaloutbox.model.Product
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

@Service
class ProductService(
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun updateProduct(): Product {
        val product = findProduct()
        val productUpdated = product.copy(
            name = "연금술사 - ${Random.nextInt(1, 100)}",
            price = Random.nextInt(10_000, 100_000).toBigDecimal(),
            updatedAt = Instant.now(),
        )
        taskService.publishOnProductUpdated(productUpdated.id)
        logger.info("Product updated: $productUpdated")
        return productUpdated
    }

    private fun findProduct(): Product {
        val createdAt = LocalDateTime.of(2025, 1, 2, 3, 4, 5)
            .toInstant(ZoneOffset.UTC)
        return Product(
            id = 10,
            name = "연금술사1",
            price = BigDecimal(10000),
            createdAt = createdAt,
            updatedAt = createdAt,
        )
    }
}