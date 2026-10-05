package com.raynor.demo.boiler.controller.coupon.dto

import com.raynor.demo.boiler.service.coupon.model.CouponModel

data class CouponResponseDto(
    val id: Long,
) {
    companion object {
        fun fromModel(model: CouponModel) =
            CouponResponseDto(
                id = model.id,
            )
    }
}
