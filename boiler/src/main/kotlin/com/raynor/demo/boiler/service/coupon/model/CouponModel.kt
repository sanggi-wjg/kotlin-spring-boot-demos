package com.raynor.demo.boiler.service.coupon.model

import com.raynor.demo.boiler.domain.coupon.Coupon
import java.time.Instant

data class CouponModel(
    val id: Long,
    val usingStartedAt: Instant,
    val usingExpiredAt: Instant,
) {
    companion object {
        fun fromEntity(coupon: Coupon): CouponModel {
            return CouponModel(
                id = coupon.id!!,
                usingStartedAt = coupon.couponScheme.usingStartedAt,
                usingExpiredAt = coupon.couponScheme.usingExpiredAt,
            )
        }
    }
}
