package com.raynor.demo.boiler.controller.support

/** 같은 HTTP 상태 코드 안에서 클라이언트가 원인을 구분해야 할 때 내려주는 에러 코드 */
enum class ErrorCode {
    /** 발급 기간이 아니거나, 일반 발급에서 수량이 소진된 쿠폰 (선착순 발급의 매진은 COUPON_SOLD_OUT) */
    COUPON_NOT_ISSUABLE,

    /** 1인 1매 정책상 이미 발급받은 쿠폰 */
    COUPON_ALREADY_ISSUED,

    /** 선착순 쿠폰 한도 도달 */
    COUPON_SOLD_OUT,
}
