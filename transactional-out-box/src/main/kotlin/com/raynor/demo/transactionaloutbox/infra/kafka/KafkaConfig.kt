package com.raynor.demo.transactionaloutbox.infra.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@EnableKafka
@Configuration
class KafkaConfig(
    private val kafkaProperty: KafkaProperty,
) {
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperty.bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to kafkaProperty.consumer.keyDeserializer,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to kafkaProperty.consumer.valueDeserializer,
            )
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            this.consumerFactory = consumerFactory()
            this.setConcurrency(1)
        }
    }
}
