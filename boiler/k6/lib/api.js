import http from 'k6/http';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const USER_ID = __ENV.USER_ID || '1';

export function createOrder(idempotencyKey, items, couponId = null) {
    return http.post(
        `${BASE_URL}/api/v1/orders`,
        JSON.stringify({ items, couponId }),
        {
            headers: {
                'Content-Type': 'application/json',
                'Idempotency-Key': idempotencyKey,
                'X-User-Id': USER_ID,
            },
            tags: { name: 'createOrder' },
        },
    );
}

export function issueCoupon(idempotencyKey, couponSchemeId, userId) {
    return http.post(
        `${BASE_URL}/api/v1/coupon-schemes/${couponSchemeId}/coupons`,
        null,
        {
            headers: {
                'Idempotency-Key': idempotencyKey,
                'X-User-Id': String(userId),
            },
            tags: { name: 'issueCoupon' },
        },
    );
}

// 선착순 쿠폰 발급 (Lua 게이트 + Kafka). 1인 1매 규칙으로 중복이 막히므로 Idempotency-Key 는 받지 않는다.
export function issueLimitedCoupon(couponSchemeId, userId) {
    return http.post(
        `${BASE_URL}/api/v1/coupon-schemes/${couponSchemeId}/limited-coupons`,
        null,
        {
            headers: {
                'X-User-Id': String(userId),
            },
            tags: { name: 'issueLimitedCoupon' },
        },
    );
}

export function uniqueKey(prefix, seq) {
    return `${prefix}-${Date.now()}-${seq}`;
}
