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

export function uniqueKey(prefix, seq) {
    return `${prefix}-${Date.now()}-${seq}`;
}
