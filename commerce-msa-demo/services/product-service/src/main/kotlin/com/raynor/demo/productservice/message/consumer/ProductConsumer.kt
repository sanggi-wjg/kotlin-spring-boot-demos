package com.raynor.demo.productservice.message.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.productservice.message.config.KafkaTopic
import com.raynor.demo.productservice.message.consumer.model.ProductReduceStockQuantityMessage
import com.raynor.demo.productservice.rds.repository.ProductRdsRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Transactional
@Service
class ProductConsumer(
    private val objectMapper: ObjectMapper,
    private val productRdsRepository: ProductRdsRepository,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = [KafkaTopic.PRODUCT_REDUCE_STOCK_QUANTITY_V1])
    fun productReduceStockQuantity(
        @Payload payload: String,
    ) {
        val message = objectMapper.readValue(payload, ProductReduceStockQuantityMessage::class.java)
        val product = productRdsRepository.findByIdWithLock(message.productId)
        if (product == null) {
            logger.warn("Product with id ${message.productId} not found")
            return
        }

        product.reduceStockQuantity(message.quantity)
    }
}
