// 1인 1매 동시성 확인 (requirements.md F4: 같은 정책의 쿠폰은 1인 1매, 동일 사용자 중복 발급 -> 409)
// 같은 사용자가 멱등 키를 각각 다르게 해서 같은 쿠폰 스킴에 REQUESTS 건을 동시에 요청한다. (더블 클릭, 여러 기기 동시 요청)
// 기대: 201 은 정확히 1건, 409(중복 발급) 는 REQUESTS - 1 건, 그 외 응답 0건
//
// 실행 전 초기화 (이전 발급 기록이 남아 있으면 전부 중복 발급 409 가 된다)
//   DELETE FROM coupon WHERE coupon_scheme_id = 2;
//   UPDATE coupon_scheme SET current_issue_count = 0 WHERE id = 2;
import {Counter} from 'k6/metrics';
import {issueCoupon, uniqueKey} from './lib/api.js';

const COUPON_SCHEME_ID = 2; // 테스트할 쿠폰 스킴 id (issue-coupon.js 와 겹치지 않게 다른 스킴 사용)
const USER_ID = 1; // 요청하는 사용자 (모든 VU 가 같은 사용자로 요청)
const REQUESTS = 20; // 동시에 보낼 요청 수

const ALREADY_ISSUED_MESSAGE = '이미 발급받은 쿠폰입니다.';

const couponIssued = new Counter('coupon_issued');
const couponAlreadyIssued = new Counter('coupon_already_issued');
const couponUnexpected = new Counter('coupon_unexpected');

export const options = {
    scenarios: {
        duplicate_issues: {
            executor: 'per-vu-iterations',
            vus: REQUESTS,
            iterations: 1,
            maxDuration: '1m',
        },
    },
    thresholds: {
        coupon_issued: ['count==1'],
        coupon_already_issued: [`count==${REQUESTS - 1}`],
        coupon_unexpected: ['count==0'],
    },
};

export default function () {
    // 멱등 키를 요청마다 다르게 해서, 멱등 처리가 아닌 1인 1매 규칙이 막는지 확인한다
    const res = issueCoupon(uniqueKey('issue-coupon-duplicate', `${__VU}-${__ITER}`), COUPON_SCHEME_ID, USER_ID);

    // 409 는 수량 소진과 중복 발급 둘 다 쓰므로 메시지로 구분한다
    if (res.status === 201) {
        couponIssued.add(1);
    } else if (res.status === 409 && res.json('message') === ALREADY_ISSUED_MESSAGE) {
        couponAlreadyIssued.add(1);
    } else {
        couponUnexpected.add(1);
        console.error(`unexpected status=${res.status} body=${res.body}`);
    }
}