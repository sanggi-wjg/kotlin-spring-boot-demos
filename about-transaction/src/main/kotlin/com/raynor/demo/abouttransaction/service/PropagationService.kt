package com.raynor.demo.abouttransaction.service

import com.raynor.demo.abouttransaction.entity.ProductEntity
import com.raynor.demo.abouttransaction.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("createProductOneTransaction: $it") }

        productRepository.save(ProductEntity(name = "상품 1 createProductOneTransaction: ${UUID.randomUUID()}"))
        productRepository.save(ProductEntity(name = "상품 2 createProductOneTransaction: ${UUID.randomUUID()}"))
        productRepository.save(ProductEntity(name = "상품 3 createProductOneTransaction: ${UUID.randomUUID()}"))

        if (raise) throw IllegalStateException("앗앗앗")
    }

    @Transactional
    fun createProductEachTransaction(raise: Boolean) {
        TransactionAspectSupport.currentTransactionStatus().hashCode().also { logger.info("createProductEachTransaction: $it") }

        kotlin.runCatching {
            requireNewService.createProductSeparately()
            requireNewService.createProductIfThrowFirst(raise = raise)
            requireNewService.createProductIfThrowLast(raise = raise)
        }.onFailure {
            logger.warn(it.message)
        }
    }
}
