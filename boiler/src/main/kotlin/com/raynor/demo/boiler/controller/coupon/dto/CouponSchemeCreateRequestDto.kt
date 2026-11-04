package com.raynor.demo.boiler.controller.coupon.dto

import com.raynor.demo.boiler.service.coupon.model.CouponSchemeCreateRequest
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
) {
    // service 는 controller 를 참조할 수 없으므로 (ArchitectureTest) 변환은 controller 계층의 DTO 가 맡는다
    fun toRequest(): CouponSchemeCreateRequest {
        return CouponSchemeCreateRequest(
            name = name,
            discountAmount = discountAmount,
            discountRate = discountRate,
            maxDiscountAmount = maxDiscountAmount,
            minOrderAmount = minOrderAmount,
            usingStartedAt = usingStartedAt,
            usingEndedAt = usingEndedAt,
            maxIssueCount = maxIssueCount,
        )
    }
}
