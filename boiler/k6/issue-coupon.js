// 선착순 쿠폰 발급 수량 정합성 확인 (requirements.md F4: 동시 발급 요청이 수량을 초과하면 초과분만 실패)
// 최대 발급 수량 MAX_ISSUE_COUNT 인 쿠폰 스킴에 서로 다른 사용자 REQUESTS 명이 동시에 발급을 요청한다.
// 기대: 201 은 정확히 MAX_ISSUE_COUNT 건, 409(발급 불가) 는 REQUESTS - MAX_ISSUE_COUNT 건, 그 외 응답 0건
//
// 실행 전 초기화 (이전 발급 기록이 남아 있으면 전부 중복 발급 409 가 된다)
//   DELETE FROM coupon WHERE coupon_scheme_id = 1;
//   UPDATE coupon_scheme SET current_issue_count = 0 WHERE id = 1;
import {Counter} from 'k6/metrics';
import {issueCoupon, uniqueKey} from './lib/api.js';

const COUPON_SCHEME_ID = 1; // 테스트할 쿠폰 스킴 id
const MAX_ISSUE_COUNT = 50; // 쿠폰 스킴의 최대 발급 수량
const REQUESTS = 100; // 동시에 요청할 사용자 수 (시드 사용자 id 1 ~ 100 을 VU 번호로 사용)

const NOT_ISSUABLE_MESSAGE = '발급할 수 없는 쿠폰입니다.';

const couponIssued = new Counter('coupon_issued');
const couponNotIssuable = new Counter('coupon_not_issuable');
const couponUnexpected = new Counter('coupon_unexpected');

export const options = {
    scenarios: {
        concurrent_issues: {
            executor: 'per-vu-iterations',
            vus: REQUESTS,
            iterations: 1,
            maxDuration: '1m',
        },
    },
    thresholds: {
        coupon_issued: [`count==${MAX_ISSUE_COUNT}`],
        coupon_not_issuable: [`count==${REQUESTS - MAX_ISSUE_COUNT}`],
        coupon_unexpected: ['count==0'],
    },
};

export default function () {
    const userId = __VU; // VU 마다 다른 사용자로 요청해 중복 발급(409)과 섞이지 않게 한다
    const res = issueCoupon(uniqueKey('issue-coupon', `${__VU}-${__ITER}`), COUPON_SCHEME_ID, userId);

    // 409 는 수량 소진과 중복 발급 둘 다 쓰므로 메시지로 구분한다
    if (res.status === 201) {
        couponIssued.add(1);
    } else if (res.status === 409 && res.json('message') === NOT_ISSUABLE_MESSAGE) {
        couponNotIssuable.add(1);
    } else {
        couponUnexpected.add(1);
        console.error(`unexpected userId=${userId} status=${res.status} body=${res.body}`);
    }
}