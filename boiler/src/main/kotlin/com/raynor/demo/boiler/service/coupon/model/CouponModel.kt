package com.raynor.demo.boiler.service.coupon.model

import com.raynor.demo.boiler.domain.coupon.Coupon

data class CouponModel(
    val id: Long,
) {
    companion object {
        fun fromEntity(coupon: Coupon): CouponModel {
            return CouponModel(
                id = coupon.id!!,
            )
        }
    }
}
