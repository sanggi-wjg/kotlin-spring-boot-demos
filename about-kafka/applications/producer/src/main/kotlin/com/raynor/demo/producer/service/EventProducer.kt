package com.raynor.demo.producer.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.shared.kafka.KafkaTopic
import com.raynor.demo.shared.kafka.message.EventMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import kotlin.random.Random

@Service
class EventProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun publishFirstScenarioEvent(): String {
        val message = publishMessage(KafkaTopic.FIRST_SCENARIO)
        return message.eventId
    }

    fun publishSecondScenarioEvent(): List<String> {
        val message1 = publishMessage(KafkaTopic.SECOND_SCENARIO)
        val message2 = publishMessage(KafkaTopic.SECOND_SCENARIO)
        val message3 = publishMessage(KafkaTopic.SECOND_SCENARIO)
        return listOf(message1.eventId, message2.eventId, message3.eventId)
    }

    fun publishThirdScenarioEvent(): String {
        val message = publishMessage(KafkaTopic.THIRD_SCENARIO)
        return message.eventId
    }

    private fun createEventMessage(): EventMessage {
        val eventId = UUID.randomUUID().toString()
        return EventMessage(
            eventId = eventId,
            message = (0..100).random().toString(),
            randomValue = Random.nextInt(100),
            timestamp = Instant.now()
        )
    }

    private fun publishMessage(topic: String): EventMessage {
        val message = createEventMessage()
        val messageJson = objectMapper.writeValueAsString(message)

        kafkaTemplate.send(topic, message.eventId, messageJson)
            .whenComplete { result, ex ->
                if (ex != null) {
                    logger.error("Failed to send message: $message, $result, $ex")
                } else {
                    logger.info("Successfully sent message: $message, $result")
                }
            }
        return message
    }
}
