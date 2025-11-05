package com.raynor.demo.consumer.kafka

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.raynor.demo.shared.kafka.KafkaConstants
import com.raynor.demo.shared.kafka.KafkaTopic
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.retrytopic.RetryTopicConfiguration
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder

@EnableKafka
@Configuration
class KafkaConfig(
    private val kafkaProperties: KafkaProperties,
) {
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(
            kafkaProperties.buildConsumerProperties()
        )
    }

    @Bean
    fun listenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            this.consumerFactory = consumerFactory()
            this.setConcurrency(kafkaProperties.listener.concurrency)
        }
    }

    @Bean
    fun retryAndDltConfig(
        kafkaTemplate: KafkaTemplate<String, String>,
        listenerContainerFactory: ConcurrentKafkaListenerContainerFactory<String, String>,
    ): RetryTopicConfiguration {
        return RetryTopicConfigurationBuilder.newInstance()
            .listenerFactory(listenerContainerFactory)
            .retryTopicSuffix(KafkaConstants.RETRY_TOPIC_SUFFIX)
            .dltSuffix(KafkaConstants.DLT_SUFFIX)
            .doNotRetryOnDltFailure()
            .dltHandlerMethod(
                KafkaDeadLetterTopicHandler.BEAN_NAME,
                KafkaDeadLetterTopicHandler.METHOD_NAME,
            )
            .notRetryOn(
                listOf(
                    JsonMappingException::class.java,
                    JsonProcessingException::class.java,
                )
            )
            .includeTopics(
                listOf(
                    KafkaTopic.FIRST_SCENARIO,
                    KafkaTopic.SECOND_SCENARIO,
                )
            )
            .create(kafkaTemplate)
    }
}
