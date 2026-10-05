package com.raynor.demo.boiler.controller.coupon.dto

import com.raynor.demo.boiler.service.coupon.model.CouponModel
import java.time.Instant

data class CouponResponseDto(
    val id: Long,
    val usingStartedAt: Instant,
    val usingExpiredAt: Instant,
) {
    companion object {
        fun fromModel(model: CouponModel) =
            CouponResponseDto(
                id = model.id,
                usingStartedAt = model.usingStartedAt,
                usingExpiredAt = model.usingExpiredAt,
            )
    }
}
