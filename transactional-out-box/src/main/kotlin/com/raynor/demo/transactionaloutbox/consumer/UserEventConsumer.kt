package com.raynor.demo.transactionaloutbox.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.transactionaloutbox.consumer.model.ProductUpdatedEvent
import com.raynor.demo.transactionaloutbox.consumer.model.UserSignedEvent
import com.raynor.demo.transactionaloutbox.enums.EventType
import com.raynor.demo.transactionaloutbox.infra.kafka.KafkaGroup
import com.raynor.demo.transactionaloutbox.infra.kafka.KafkaTopic
import com.raynor.demo.transactionaloutbox.repository.OutboxRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserEventConsumer(
    private val objectMapper: ObjectMapper,
    private val outboxRepository: OutboxRepository,
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
        /*
        2024-08-17T21:03:52.410+09:00  INFO 16257 --- [sat] [ntainer#0-0-C-1] c.r.d.t.consumer.UserEventConsumer       : outboxId: 22
        2024-08-17T21:03:52.410+09:00  INFO 16257 --- [sat] [ntainer#0-0-C-1] c.r.d.t.consumer.UserEventConsumer       : eventType: USER_SIGNED
        2024-08-17T21:03:52.410+09:00  INFO 16257 --- [sat] [ntainer#0-0-C-1] c.r.d.t.consumer.UserEventConsumer       : event: {"schema":{"type":"string","optional":false,"name":"io.debezium.data.Json","version":1},"payload":"{\"id\":5117,\"name\":\"2b316917-98be-4062-b394-6f2cbc8c8c45\",\"email\":\"2b316917-98be-4062-b394-6f2cbc8c8c45@dev.com\"}"}
        2024-08-17T21:03:52.412+09:00  INFO 16257 --- [sat] [ntainer#0-0-C-1] c.r.d.t.consumer.UserEventConsumer       : payload: {"id":5117,"name":"2b316917-98be-4062-b394-6f2cbc8c8c45","email":"2b316917-98be-4062-b394-6f2cbc8c8c45@dev.com"}
        2024-08-17T21:03:52.441+09:00  INFO 16257 --- [sat] [ntainer#0-0-C-1] c.r.d.t.consumer.UserEventConsumer       : userSignedEvent: UserSignedEvent(name=2b316917-98be-4062-b394-6f2cbc8c8c45, email=2b316917-98be-4062-b394-6f2cbc8c8c45@dev.com)
        * */
        logger.info("outboxId: $outboxId")
        logger.info("eventType: $eventType")
        logger.info("event: $event")
        val payload = getPayload(event)
        logger.info("payload: $payload")

        fire(outboxId.toLong()) {
            when (eventType) {
                EventType.USER_SIGNED.name -> {
                    val userSignedEvent = objectMapper.readValue(payload, UserSignedEvent::class.java)
                    logger.info("userSignedEvent: $userSignedEvent")
                }
            }
        }
    }

    @KafkaListener(
        topics = [KafkaTopic.PRODUCT_EVENTS],
        groupId = KafkaGroup.SPRING_TOB,
    )
    fun consumerProductEvents(
        @Header(value = "id") outboxId: String,
        @Header(value = "eventType") eventType: String,
        @Payload event: String,
    ) {
        /*
        2024-08-17T21:03:56.407+09:00  INFO 16257 --- [sat] [ntainer#1-0-C-1] c.r.d.t.consumer.UserEventConsumer       : outboxId: 23
        2024-08-17T21:03:56.407+09:00  INFO 16257 --- [sat] [ntainer#1-0-C-1] c.r.d.t.consumer.UserEventConsumer       : eventType: PRODUCT_UPDATED
        2024-08-17T21:03:56.407+09:00  INFO 16257 --- [sat] [ntainer#1-0-C-1] c.r.d.t.consumer.UserEventConsumer       : event: {"schema":{"type":"string","optional":false,"name":"io.debezium.data.Json","version":1},"payload":"{\"id\":2013}"}
        2024-08-17T21:03:56.407+09:00  INFO 16257 --- [sat] [ntainer#1-0-C-1] c.r.d.t.consumer.UserEventConsumer       : payload: {"id":2013}
        2024-08-17T21:03:56.411+09:00  INFO 16257 --- [sat] [ntainer#1-0-C-1] c.r.d.t.consumer.UserEventConsumer       : productUpdatedEvent: ProductUpdatedEvent(id=2013)
        * */
        logger.info("outboxId: $outboxId")
        logger.info("eventType: $eventType")
        logger.info("event: $event")
        val payload = getPayload(event)
        logger.info("payload: $payload")

        fire(outboxId.toLong()) {
            when (eventType) {
                EventType.PRODUCT_UPDATED.name -> {
                    val productUpdatedEvent = objectMapper.readValue(payload, ProductUpdatedEvent::class.java)
                    logger.info("productUpdatedEvent: $productUpdatedEvent")
                }
            }
        }
    }

    private fun fire(outboxId: Long, function: () -> Unit) {
        val outbox = outboxRepository.findByIdOrNull(outboxId)
            ?: throw EntityNotFoundException("Outbox not found: $outboxId")
        function.invoke()
        outbox.done()
        outboxRepository.save(outbox)
    }

    private fun getPayload(event: String): String {
        return objectMapper.readTree(event).get("payload").asText()
    }
}
