package com.raynor.demo.boiler.infra.kafka

import com.raynor.demo.boiler.infra.kafka.event.LimitedCouponIssueEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

interface KafkaProducer {
    fun publishLimitedCouponIssueEvent(event: LimitedCouponIssueEvent)
}

@Component
class DefaultKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
) : KafkaProducer {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishLimitedCouponIssueEvent(event: LimitedCouponIssueEvent) {
        val topic = KafkaTopic.LIMITED_COUPON_ISSUE
        val key = event.couponSchemeId.toString()
        val message = objectMapper.writeValueAsString(event)
        // 키를 couponSchemeId 로 했을 때 couponSchemeId 당 파티션 1개만 사용하지만 컨슈머에서 락 경합이나 동시 이벤트시 유용할 수 있음
        // 키를 userId 로 했을 때 파티션 분산 처리와 혹시 모르는 중복 이슈를 방지할 수 있음 (멱등성에 유리할 지도?)

        runCatching {
            kafkaTemplate.send(topic, key, message)
                .whenComplete { _, ex ->
                    if (ex != null) {
                        log.error("Failed to publish event to Kafka. topic={}, message={}", topic, message, ex)
                    }
                }
        }.onFailure { ex ->
            log.error("Failed to publish event to Kafka. topic={}, message={}", topic, message, ex)
        }
    }
}
