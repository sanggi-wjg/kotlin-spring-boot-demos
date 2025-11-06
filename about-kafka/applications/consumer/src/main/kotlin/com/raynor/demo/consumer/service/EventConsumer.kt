package com.raynor.demo.consumer.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.consumer.mysql.entity.EventEntity
import com.raynor.demo.consumer.mysql.repository.EventRepository
import com.raynor.demo.shared.kafka.KafkaGroup
import com.raynor.demo.shared.kafka.KafkaTopic
import com.raynor.demo.shared.kafka.message.EventMessage
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
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
        topics = [KafkaTopic.FIRST_SCENARIO],
        groupId = KafkaGroup.CONSUMER_GROUP_ID,
    )
    fun consumeFirstScenarioEvent(@Payload messageJson: String) {
        val message = objectMapper.readValue(messageJson, EventMessage::class.java)

        eventRepository.findByEventId(message.eventId)?.let {
            logger.warn("🔥 FirstScenarioEvent EventId ${message.eventId} 중복 발생")
            throw RuntimeException("EventId ${message.eventId} 중복 발생")
        }

        EventEntity(
            eventId = message.eventId,
            message = message.message,
            timestamp = message.timestamp,
        ).let {
            eventRepository.save(it)
            logger.info("😎 FirstScenarioEvent 생성 완료. Entity(id=${it.id}, eventId=${it.eventId})")
        }
    }

    @KafkaListener(
        topics = [KafkaTopic.SECOND_SCENARIO],
        groupId = KafkaGroup.CONSUMER_GROUP_ID,
        containerFactory = "manualAckKafkaListenerContainerFactory"
    )
    fun consumeSecondScenarioEvent(@Payload messageJson: String, ack: Acknowledgment) {
        try {
            val message = objectMapper.readValue(messageJson, EventMessage::class.java)

            Thread.sleep(3000)
            eventRepository.findByEventId(message.eventId)?.let {
                logger.warn("🔥 SecondScenarioEvent EventId ${message.eventId} 중복 발생")
                throw IllegalStateException("EventId ${message.eventId} 중복 발생")
            }

            EventEntity(
                eventId = message.eventId,
                message = message.message,
                timestamp = message.timestamp,
            ).let {
                eventRepository.save(it)
                logger.info("😎 SecondScenarioEvent 생성 완료. Entity(id=${it.id}, eventId=${it.eventId})")
            }
            ack.acknowledge()
        } catch (e: IllegalStateException) {
            logger.warn("😢 SecondScenarioEvent IllegalStateException: {}", e.message)
            ack.acknowledge()
        } catch (e: Exception) {
            logger.error("❌ SecondScenarioEvent 생성 실패. Exception: {}", e.message)
            throw e
        }
    }
}
