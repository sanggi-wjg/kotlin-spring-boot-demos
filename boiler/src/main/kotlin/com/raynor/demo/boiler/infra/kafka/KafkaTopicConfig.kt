package com.raynor.demo.boiler.infra.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    companion object {
        private const val DEFAULT_PARTITIONS = 10
        private const val DEFAULT_REPLICAS = 1
    }

    @Bean
    fun limitedCouponIssueTopic(): NewTopic {
        return TopicBuilder
            .name(KafkaTopic.LIMITED_COUPON_ISSUE)
            .replicas(DEFAULT_REPLICAS)
            .partitions(DEFAULT_PARTITIONS)
            .build()
    }
}
