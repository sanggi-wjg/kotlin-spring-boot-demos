package com.raynor.demo.abouttransaction.service.transaction

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
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("NewSeparately: $it") }

        productRepository.save(ProductEntity(name = "NewSeparately: ${UUID.randomUUID()}"))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createProductIfThrowHead(raise: Boolean = false) {
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("NewThrowHead: $it") }
        if (raise) throw IllegalStateException("NewThrowHead")

        productRepository.save(ProductEntity(name = "NewThrowHead: ${UUID.randomUUID()}"))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createProductIfThrowTail(raise: Boolean = false) {
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("NewThrowTail: $it") }

        productRepository.save(ProductEntity(name = "NewThrowTail: ${UUID.randomUUID()}"))
        if (raise) throw IllegalStateException("NewThrowTail")
    }
}