package com.raynor.demo.shared.enums

enum class OrderStatus {
    PENDING, // 주문 접수
    PAYMENT_APPROVED, // 결제 승인
    CONFIRMED, // 주문 확정(승인)
    SHIPPING, // 배송 중
    DELIVERED, // 배송 완료
    CANCELLED, // 취소
}
