package com.raynor.demo.boiler.controller.coupon.dto

data class IssueLimitedCouponResponseDto(
    val couponSchemeId: Int,
    // 발급(DB 저장)은 consumer 에서 비동기로 처리되므로 이 시점에는 요청 접수만 된 상태
    val message: String = "Limited coupon issue request accepted",
)
