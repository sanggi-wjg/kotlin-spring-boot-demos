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
class PropagationRequiresNewService(
    private val productRepository: ProductRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createProductSeparately() {
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("createProductSeparately: $it") }

        productRepository.save(ProductEntity(name = "상품 createProductSeparately: ${UUID.randomUUID()}"))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createProductIfThrowFirst(raise: Boolean = false) {
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("createProductIfThrowFirst: $it") }
        if (raise) throw IllegalStateException("exception")

        productRepository.save(ProductEntity(name = "상품 createProductIfThrowFirst: ${UUID.randomUUID()}"))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createProductIfThrowLast(raise: Boolean = false) {
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("createProductIfThrowLast: $it") }

        productRepository.save(ProductEntity(name = "상품 createProductIfThrowLast: ${UUID.randomUUID()}"))
        if (raise) throw IllegalStateException("exception")
    }
}