package com.raynor.demo.boiler.service.coupon.model

import com.raynor.demo.boiler.domain.coupon.CouponScheme
import com.raynor.demo.boiler.domain.coupon.DiscountType
import java.math.BigDecimal
import java.time.Instant

data class CouponSchemeModel(
    val id: Int,
    val name: String,
    val discountType: DiscountType,
    val discountAmount: BigDecimal?,
    val discountRate: BigDecimal?,
    val maxDiscountAmount: BigDecimal?,
    val minOrderAmount: BigDecimal,
    val usingStartedAt: Instant,
    val usingExpiredAt: Instant,
    val maxIssueCount: Int,
) {
    companion object {
        fun fromEntity(entity: CouponScheme): CouponSchemeModel {
            return CouponSchemeModel(
                id = entity.id!!,
                name = entity.name,
                discountType = entity.discountType,
                discountAmount = entity.discountAmount?.amount,
                discountRate = entity.discountRate,
                maxDiscountAmount = entity.maxDiscountAmount?.amount,
                minOrderAmount = entity.minOrderAmount.amount,
                usingStartedAt = entity.usingStartedAt,
                usingExpiredAt = entity.usingExpiredAt,
                maxIssueCount = entity.maxIssueCount,
            )
        }
    }

    val isIssuable: Boolean
        get() = Instant.now() in usingStartedAt..<usingExpiredAt
}
