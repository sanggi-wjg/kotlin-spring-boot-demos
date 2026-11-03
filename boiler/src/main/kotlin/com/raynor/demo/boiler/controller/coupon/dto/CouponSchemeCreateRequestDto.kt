package com.raynor.demo.boiler.controller.coupon.dto

import jakarta.validation.constraints.Min
import java.math.BigDecimal
import java.time.Instant

data class CouponSchemeCreateRequestDto(
    val name: String,
    val discountAmount: BigDecimal?,
    val discountRate: BigDecimal?,
    val maxDiscountAmount: BigDecimal?,
    val minOrderAmount: BigDecimal,
    val usingStartedAt: Instant,
    val usingEndedAt: Instant,
    @Min(1)
    val maxIssueCount: Int,
)
