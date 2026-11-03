package com.raynor.demo.boiler.controller.coupon.dto

import com.raynor.demo.boiler.service.coupon.model.CouponSchemeModel
import java.math.BigDecimal
import java.time.Instant

data class CouponSchemeResponseDto(
    val id: Int,
    val name: String,
    val discountAmount: BigDecimal?,
    val discountRate: BigDecimal?,
    val maxDiscountAmount: BigDecimal?,
    val minOrderAmount: BigDecimal,
    val usingStartedAt: Instant,
    val usingExpiredAt: Instant,
    val maxIssueCount: Int,
) {
    companion object {
        fun fromModel(model: CouponSchemeModel): CouponSchemeResponseDto {
            return CouponSchemeResponseDto(
                id = model.id,
                name = model.name,
                discountAmount = model.discountAmount,
                discountRate = model.discountRate,
                maxDiscountAmount = model.maxDiscountAmount,
                minOrderAmount = model.minOrderAmount,
                usingStartedAt = model.usingStartedAt,
                usingExpiredAt = model.usingExpiredAt,
                maxIssueCount = model.maxIssueCount,
            )
        }
    }
}
