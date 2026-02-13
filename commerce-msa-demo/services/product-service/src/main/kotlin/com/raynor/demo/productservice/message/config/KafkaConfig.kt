package com.raynor.demo.productservice.message.config

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.retrytopic.RetryTopicConfiguration
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder

@Configuration
@EnableKafka
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
            this.setConcurrency(KafkaConstants.CONCURRENCY)
        }
    }

    @Bean
    fun retryAndDltConfig(
        kafkaTemplate: KafkaTemplate<String, String>,
        listenerContainerFactory: ConcurrentKafkaListenerContainerFactory<String, String>,
    ): RetryTopicConfiguration {
        return RetryTopicConfigurationBuilder.newInstance()
            .listenerFactory(listenerContainerFactory)
            .maxAttempts(KafkaConstants.Retry.MAX_ATTEMPTS)
            .exponentialBackoff(
                KafkaConstants.Retry.BACKOFF_INITIAL_DELAY,
                KafkaConstants.Retry.BACKOFF_MULTIPLIER,
                KafkaConstants.Retry.BACKOFF_MAX_DELAY,
            )
            .retryTopicSuffix(KafkaConstants.RETRY_TOPIC_SUFFIX)
            .dltSuffix(KafkaConstants.DLT_SUFFIX)
            .doNotRetryOnDltFailure()
            .dltHandlerMethod(
                KafkaDeadLetterTopicHandler.BEAN_NAME,
                KafkaDeadLetterTopicHandler.METHOD_NAME,
            )
//            .notRetryOn()
//            .includeTopics()
            .create(kafkaTemplate)
    }
}
