// 주문 생성 응답 시간 측정 (requirements.md N3)
// 측정 조건: 로컬 단일 인스턴스, 동시 사용자 50, 워밍업 30초 후 60초 측정, 상품 100건과 재고 충분 상태
// 목표: 주문 생성은 락 대기를 포함해 p95 500ms 이내
import {check} from 'k6';
import {createOrder, uniqueKey} from './lib/api.js';

const VUS = 50;
const PRODUCT_COUNT = 100; // 시드 데이터 상품 id 1 ~ 100

export const options = {
    scenarios: {
        warmup: {
            executor: 'constant-vus',
            vus: VUS,
            duration: '30s',
        },
        measure: {
            executor: 'constant-vus',
            vus: VUS,
            duration: '60s',
            startTime: '30s', // 워밍업이 끝난 뒤 시작
        },
    },
    thresholds: {
        // 워밍업 구간은 제외하고 measure 시나리오만 판정한다
        'http_req_duration{scenario:measure}': ['p(95)<500'],
        'checks{scenario:measure}': ['rate==1.0'],
    },
};

export default function () {
    const productId = Math.floor(Math.random() * PRODUCT_COUNT) + 1;
    const res = createOrder(
        uniqueKey('create-order', `${__VU}-${__ITER}`),
        [{productId, quantity: 1}],
    );

    check(res, {
        'status is 201': (r) => r.status === 201,
    });
}