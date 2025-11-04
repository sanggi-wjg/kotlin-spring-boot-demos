package com.raynor.demo.producer.kafka

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@EnableKafka
@Configuration
class KafkaConfig(
    private val kafkaProperties: KafkaProperties,
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        return DefaultKafkaProducerFactory(
            kafkaProperties.buildProducerProperties()
        )
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }
}
