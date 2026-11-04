package com.raynor.demo.boiler.service.coupon

import com.raynor.demo.boiler.infra.kafka.KafkaGroupId
import com.raynor.demo.boiler.infra.kafka.KafkaTopic
import com.raynor.demo.boiler.infra.kafka.event.LimitedCouponIssueEvent
import com.raynor.demo.boiler.shared.exception.AlreadyIssuedCouponException
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
class CouponConsumer(
    private val objectMapper: ObjectMapper,
    private val couponService: CouponService,
) {

    private val log = LoggerFactory.getLogger(CouponConsumer::class.java)

    @KafkaListener(
        topics = [KafkaTopic.LIMITED_COUPON_ISSUE],
        groupId = KafkaGroupId.LIMITED_COUPON_ISSUE,
    )
    fun consumeLimitedCouponIssue(message: String) {
        val event = objectMapper.readValue(message, LimitedCouponIssueEvent::class.java)
        try {
            couponService.issueCoupon(event.couponSchemeId, event.userId)
        } catch (e: AlreadyIssuedCouponException) {
            log.warn("Coupon already issued. userId={}, couponSchemeId={}", event.userId, event.couponSchemeId)
        }
    }
}
