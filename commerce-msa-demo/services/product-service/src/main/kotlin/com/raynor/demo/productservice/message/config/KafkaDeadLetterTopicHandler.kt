package com.raynor.demo.productservice.message.config

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@ConditionalOnClass(KafkaTemplate::class)
@Component(KafkaDeadLetterTopicHandler.BEAN_NAME)
class KafkaDeadLetterTopicHandler {

    companion object {
        const val BEAN_NAME = "kafkaDeadLetterTopicHandler"
        const val METHOD_NAME = "handle"
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun handle(
        @Header(KafkaHeaders.ORIGINAL_TOPIC, required = false) originalTopic: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC, required = false) receivedTopic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION, required = false) receivedPartitionId: Int,
        @Header(KafkaHeaders.OFFSET, required = false) offset: Int,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE, required = false) exceptionMessage: String,
        consumerRecord: ConsumerRecord<String, String>,
        @Payload message: String,
    ) {
        mapOf(
            "originalTopic" to originalTopic,
            "receivedTopic" to receivedTopic,
            "receivedPartitionId" to receivedPartitionId,
            "offset" to offset,
            "exceptionMessage" to exceptionMessage,
            "consumerRecord" to consumerRecord,
            "message" to message,
        ).let {
            logger.error("❌ Failed to consume message of topic\n: $it")
        }
    }
}
