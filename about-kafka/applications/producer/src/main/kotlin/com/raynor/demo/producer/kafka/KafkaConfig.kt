package com.raynor.demo.producer.kafka

import com.raynor.demo.shared.kafka.KafkaConstants
import com.raynor.demo.shared.kafka.KafkaTopic
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@EnableKafka
@Configuration
class KafkaConfig(
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        private const val DEFAULT_PARTITIONS = 30
        private const val DEFAULT_REPLICAS = 3
    }

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
    fun kafkaAdmin(): KafkaAdmin {
        return KafkaAdmin(
            mapOf(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers
            )
        ).apply {
            this.setAutoCreate(true)
        }
    }

    @Bean
    fun firstScenarioTopic(): NewTopic = createTopic(KafkaTopic.FIRST_SCENARIO)

    @Bean
    fun firstScenarioRetry0Topic(): NewTopic = createRetryTopic(KafkaTopic.FIRST_SCENARIO, 0)

    @Bean
    fun firstScenarioRetry1Topic(): NewTopic = createRetryTopic(KafkaTopic.FIRST_SCENARIO, 1)

    @Bean
    fun firstScenarioDltTopic(): NewTopic = createDltTopic(KafkaTopic.FIRST_SCENARIO)

    @Bean
    fun secondScenarioTopic(): NewTopic = createTopic(KafkaTopic.SECOND_SCENARIO)

    private fun createTopic(
        name: String,
        partitions: Int = DEFAULT_PARTITIONS,
        replicas: Int = DEFAULT_REPLICAS
    ): NewTopic =
        TopicBuilder.name(name)
            .partitions(partitions)
            .replicas(replicas)
            .compact()
            .build()

    private fun createRetryTopic(
        baseTopic: String,
        retryLevel: Int,
        partitions: Int = DEFAULT_PARTITIONS,
        replicas: Int = DEFAULT_REPLICAS
    ): NewTopic =
        createTopic("$baseTopic${KafkaConstants.RETRY_TOPIC_SUFFIX}-$retryLevel", partitions, replicas)

    private fun createDltTopic(
        baseTopic: String,
        partitions: Int = DEFAULT_PARTITIONS,
        replicas: Int = DEFAULT_REPLICAS
    ): NewTopic =
        createTopic("$baseTopic${KafkaConstants.DLT_SUFFIX}", partitions, replicas)
}
