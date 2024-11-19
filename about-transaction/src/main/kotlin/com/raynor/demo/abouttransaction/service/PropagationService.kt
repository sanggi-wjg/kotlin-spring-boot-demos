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
class PropagationService(
    private val productRepository: ProductRepository,
    private val requireNewService: PropagationRequiresNewService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun createProductOneTransaction(raise: Boolean) {
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("OneTransaction: $it") }

        productRepository.save(ProductEntity(name = "OneTransaction 1: ${UUID.randomUUID()}"))
        productRepository.save(ProductEntity(name = "OneTransaction 2: ${UUID.randomUUID()}"))
        productRepository.save(ProductEntity(name = "OneTransaction 3: ${UUID.randomUUID()}"))

        if (raise) throw IllegalStateException("앗앗앗")
    }

    @Transactional
    fun createProductEachTransaction(raise: Boolean) {
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("EachTransaction: $it") }

        kotlin.runCatching {
            requireNewService.createProductSeparately()
            requireNewService.createProductIfThrowHead(raise = raise)
            requireNewService.createProductIfThrowTail(raise = raise)

            createProductIfThrowHead(raise = raise)
            createProductIfThrowTail(raise = raise)
            createProductWithNewTxIfThrowHead(raise = raise)
            createProductWithNewTxIfThrowTail(raise = raise)
        }.onFailure {
            logger.warn(it.message)
        }
    }

    @Transactional
    fun createProductIfThrowHead(raise: Boolean) {
        if (raise) throw IllegalStateException("HeadThrow exception")
        productRepository.save(ProductEntity(name = "HeadThrow: ${UUID.randomUUID()}"))
    }

    @Transactional
    fun createProductIfThrowTail(raise: Boolean) {
        productRepository.save(ProductEntity(name = "TailThrow: ${UUID.randomUUID()}"))
        if (raise) throw IllegalStateException("TailThrow exception")
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createProductWithNewTxIfThrowHead(raise: Boolean) {
        if (raise) throw IllegalStateException("NewTxIfThrowHead exception")
        productRepository.save(ProductEntity(name = "NewTxIfThrowHead: ${UUID.randomUUID()}"))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createProductWithNewTxIfThrowTail(raise: Boolean) {
        productRepository.save(ProductEntity(name = "NewTxIfThrowTail: ${UUID.randomUUID()}"))
        if (raise) throw IllegalStateException("NewTxIfThrowTail exception")
    }
}
