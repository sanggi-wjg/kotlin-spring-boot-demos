package com.raynor.demo.boiler.infra.kafka.event

data class LimitedCouponIssueEvent(
    val couponSchemeId: Int,
    val userId: Int,
)
