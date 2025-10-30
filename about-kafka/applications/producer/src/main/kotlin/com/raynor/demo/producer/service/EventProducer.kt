package com.raynor.demo.producer.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.shared.kafka.KafkaTopicName
import com.raynor.demo.shared.kafka.message.FirstScenarioEventMessage
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
        val eventId = UUID.randomUUID().toString()
        val message = FirstScenarioEventMessage(
            eventId = eventId,
            message = (0..100).random().toString(),
            randomValue = Random.nextInt(101),
            timestamp = Instant.now()
        )
        val messageJson = objectMapper.writeValueAsString(message)

        kafkaTemplate.send(KafkaTopicName.FIRST_SCENARIO, eventId, messageJson)
            .whenComplete { result, ex ->
                if (ex != null) {
                    logger.error("Failed to send message: $message, $result, $ex")
                }
            }
        return eventId
    }
}
