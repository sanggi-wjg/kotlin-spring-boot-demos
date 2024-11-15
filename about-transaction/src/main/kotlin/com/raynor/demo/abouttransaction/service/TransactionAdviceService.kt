package com.raynor.demo.abouttransaction.service

import com.raynor.demo.abouttransaction.config.advice.Transaction
import com.raynor.demo.abouttransaction.entity.ProductEntity
import com.raynor.demo.abouttransaction.repository.CategoryRepository
import com.raynor.demo.abouttransaction.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport

@Service
class TransactionAdviceService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) {
    private val logger = LoggerFactory.getLogger(TransactionAdviceService::class.java)

    @Transactional
    fun something() {
        logger.info("zero TransactionId: ${TransactionAspectSupport.currentTransactionStatus().hashCode()}")
        val categories = categoryRepository.findAll()
        println(categories)

        Transaction.writeable {
            val txId = TransactionAspectSupport.currentTransactionStatus().hashCode()
            logger.info("first TransactionId: $txId")

        }

        Transaction.writeable {
            val txId = TransactionAspectSupport.currentTransactionStatus().hashCode()
            logger.info("second TransactionId: $txId")
            into()
        }

        val p1 = into2()
        println(p1)
        val p2 = into2()
        println(p2)

        val p3 = into3()
        println(p3)
    }

    fun into() {
        val products = productRepository.findAll()
        println(products)
    }

    fun into2(): MutableList<ProductEntity> = Transaction.writeable {
        val txId = TransactionAspectSupport.currentTransactionStatus().hashCode()
        logger.info("into2 method TransactionId: $txId")

        return@writeable productRepository.findAll()
    }

    fun into3(): MutableList<ProductEntity> = Transaction.readonly {
        val txId = TransactionAspectSupport.currentTransactionStatus().hashCode()
        logger.info("into3 method TransactionId: $txId")

        return@readonly productRepository.findAll()
    }
}