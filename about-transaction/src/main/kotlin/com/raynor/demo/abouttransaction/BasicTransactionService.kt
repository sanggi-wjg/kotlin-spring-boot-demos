package com.raynor.demo.abouttransaction

import com.raynor.demo.abouttransaction.entity.ProductEntity
import com.raynor.demo.abouttransaction.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import kotlin.random.Random

@Service
class BasicTransactionService(
    private val productRepository: ProductRepository,
) {
    // todo lambda trailing 구현으로 transaction 분리

    @Transactional
    fun insertWithBasicTransaction(): ProductEntity {
        return ProductEntity(
            id = null,
            name = "상품 - ${UUID.randomUUID()}",
            price = Random.nextInt(1, 100).toBigDecimal(),
            stockQuantity = Random.nextInt(1, 100),
            createdAt = Instant.now(),
        ).let {
            productRepository.save(it)
        }
    }

    @Transactional
    fun getList(): List<ProductEntity> {
        return productRepository.findAll()
    }
}
