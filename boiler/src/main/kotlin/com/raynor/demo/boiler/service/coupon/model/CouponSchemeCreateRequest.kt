package com.raynor.demo.boiler.service.coupon.model

import com.raynor.demo.boiler.domain.coupon.DiscountType
import java.math.BigDecimal
import java.time.Instant

data class CouponSchemeCreateRequest(
    val name: String,
    val discountAmount: BigDecimal?,
    val discountRate: BigDecimal?,
    val maxDiscountAmount: BigDecimal?,
    val minOrderAmount: BigDecimal,
    val usingStartedAt: Instant,
    val usingEndedAt: Instant,
    val maxIssueCount: Int,
) {
    val discountType = if (discountAmount == null) DiscountType.RATE else DiscountType.AMOUNT
}
