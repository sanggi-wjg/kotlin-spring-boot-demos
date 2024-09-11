package com.raynor.demo.abouttransaction.service

import com.raynor.demo.abouttransaction.entity.ProductEntity
import com.raynor.demo.abouttransaction.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport
import java.util.*

@Service
class PropagationTransactionService(
    private val productRepository: ProductRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun insertWithNewTransactionAndMainMethodException(): List<ProductEntity> {
        val product2 = insert2()
        val product3 = insert3()

        throw Exception("exception")

        return listOf(
            productRepository.save(
                ProductEntity(
//                    id = null,
                    name = "상품 1 ${UUID.randomUUID()}",
//                    price = Random.nextInt(1, 100).toBigDecimal(),
//                    stockQuantity = Random.nextInt(1, 100),
//                    createdAt = Instant.now(),
                )
            ),
            product2,
            product3
        )
    }

    @Transactional
    fun insertWithNewTransactionAndSubMethodException(): List<ProductEntity> {
        val product1 = productRepository.save(
            ProductEntity(
//                id = null,
                name = "상품 1 ${UUID.randomUUID()}",
//                price = Random.nextInt(1, 100).toBigDecimal(),
//                stockQuantity = Random.nextInt(1, 100),
//                createdAt = Instant.now(),
            )
        )
        val product2 = insert2()
        val product3 = insert3(raiseException = true)

        return listOf(
            product1,
            product2,
            product3
        )
    }

    @Transactional
    fun insertWithNewTransaction(): List<ProductEntity> {
        val transactionId = TransactionAspectSupport.currentTransactionStatus().hashCode()
        logger.info("insertWithNewTransaction method TransactionId= $transactionId")

        val product1 = productRepository.save(
            ProductEntity(
//                id = null,
                name = "상품 1 ${UUID.randomUUID()}",
//                price = Random.nextInt(1, 100).toBigDecimal(),
//                stockQuantity = Random.nextInt(1, 100),
//                createdAt = Instant.now(),
            )
        )
        val product2 = insert2()
        val product3 = insert3(raiseException = false)

        return listOf(
            product1,
            product2,
            product3
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun insert2(): ProductEntity {
        return productRepository.save(
            ProductEntity(
//                id = null,
                name = "상품 2 ${UUID.randomUUID()}",
//                price = Random.nextInt(1, 100).toBigDecimal(),
//                stockQuantity = Random.nextInt(1, 100),
//                createdAt = Instant.now(),
            )
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun insert3(raiseException: Boolean = false): ProductEntity {
        val transactionId = TransactionAspectSupport.currentTransactionStatus().hashCode()
        logger.info("insert3 method TransactionId= $transactionId")

        return productRepository.save(
            ProductEntity(
//                id = null,
                name = "상품 3 ${UUID.randomUUID()}",
//                price = Random.nextInt(1, 100).toBigDecimal(),
//                stockQuantity = Random.nextInt(1, 100),
//                createdAt = Instant.now(),
            )
        ).also {
            if (raiseException) {
                throw Exception("exception")
            }
        }
    }
}
