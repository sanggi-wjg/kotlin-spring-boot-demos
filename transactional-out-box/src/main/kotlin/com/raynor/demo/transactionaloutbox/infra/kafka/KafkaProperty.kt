package com.raynor.demo.transactionaloutbox.infra.kafka

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties

@ConditionalOnProperty(prefix = "config.kafka", name = ["bootstrap-servers"])
@ConfigurationProperties(prefix = "config.kafka")
data class KafkaProperty(
    val bootstrapServers: String,
    val consumer: ConsumerProperty,
) {
    data class ConsumerProperty(
        val autoOffsetReset: String,
        val keyDeserializer: String,
        val valueDeserializer: String,
    )
}
