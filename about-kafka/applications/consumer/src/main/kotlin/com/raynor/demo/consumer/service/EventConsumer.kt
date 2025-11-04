package com.raynor.demo.consumer.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.consumer.mysql.entity.EventEntity
import com.raynor.demo.consumer.mysql.repository.EventRepository
import com.raynor.demo.shared.kafka.KafkaGroup
import com.raynor.demo.shared.kafka.KafkaTopicName
import com.raynor.demo.shared.kafka.message.EventMessage
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Transactional
@Service
class EventConsumer(
    private val eventRepository: EventRepository,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = [KafkaTopicName.FIRST_SCENARIO],
        groupId = KafkaGroup.CONSUMER_GROUP_ID,
    )
    fun consumeFirstScenarioEvent(@Payload messageJson: String) {
        val message = objectMapper.readValue(messageJson, EventMessage::class.java)

//        if (message.randomValue == 100) {
//            throw IllegalArgumentException("백점 만점에 백점인가")
//        }
        eventRepository.findByEventId(message.eventId)?.let {
            logger.warn("🔥 EventId ${message.eventId} 중복 발생")
            throw RuntimeException("EventId ${message.eventId} 중복 발생")
        }

        EventEntity(
            eventId = message.eventId,
            message = message.message,
            timestamp = message.timestamp,
        ).let {
            eventRepository.save(it)
            logger.info("FirstScenarioEvent 생성 완료. Entity(id=${it.id}, eventId=${it.eventId})")
        }
    }
}
