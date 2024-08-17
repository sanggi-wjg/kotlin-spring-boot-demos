package com.raynor.demo.transactionaloutbox.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.transactionaloutbox.consumer.model.UserSignedEvent
import com.raynor.demo.transactionaloutbox.entity.EventType
import com.raynor.demo.transactionaloutbox.infra.kafka.KafkaGroup
import com.raynor.demo.transactionaloutbox.infra.kafka.KafkaTopic
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserEventConsumer(
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = [KafkaTopic.USER_EVENTS],
        groupId = KafkaGroup.SPRING_TOB,
    )
    fun consumerUserEvents(
        @Header(value = "id") outboxId: String,
        @Header(value = "eventType") eventType: String,
        @Payload event: String,
    ) {
        logger.info("outboxId: $outboxId")
        logger.info("eventType: $eventType")
        logger.info("event: $event")
        val payload = getPayload(event)
        logger.info("payload: $payload")

        when (eventType) {
            EventType.USER_SIGNED.name -> {
                val userSignedEvent = objectMapper.readValue(payload, UserSignedEvent::class.java)
                logger.info("userSignedEvent: $userSignedEvent")
            }
        }
    }

    private fun getPayload(event: String): String {
        return objectMapper.readTree(event).get("payload").asText()
    }
}
