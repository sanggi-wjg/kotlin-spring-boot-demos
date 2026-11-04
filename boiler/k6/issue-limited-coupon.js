// 선착순 쿠폰 발급 (Lua 게이트 + Kafka) 수량 정합성·응답 시간 확인
// 최대 발급 수량 MAX_ISSUE_COUNT 인 쿠폰 스킴에 서로 다른 사용자 REQUESTS 명이 VUS 개의 VU 로 나눠 발급을 요청한다.
// 기대: 202 는 정확히 MAX_ISSUE_COUNT 건, 409(COUPON_SOLD_OUT) 는 REQUESTS - MAX_ISSUE_COUNT 건, 그 외 응답 0건
// 202 는 접수만 의미하므로, 실행 후 consumer lag 가 0 이 된 뒤 coupon 테이블 건수까지 확인해야 검증이 끝난다.
//
// 실행: k6 run -e COUPON_SCHEME_ID=2 -e MAX_ISSUE_COUNT=50 -e REQUESTS=10000 -e VUS=200 k6/issue-limited-coupon.js
//   (사용자 id 1 ~ REQUESTS 가 DB 에 있어야 한다. 없는 사용자도 게이트는 통과하지만 consumer 에서 발급에 실패한다.)
//
// 실행 전 초기화
//   DELETE FROM coupon WHERE coupon_scheme_id = {id};
//   UPDATE coupon_scheme SET current_issue_count = 0 WHERE id = {id};
//   redis-cli DEL limited-coupon-issue:{id}
//   redis-cli DEL coupon-scheme::{id}   (DB 의 max_issue_count 를 바꿨다면 캐시된 한도도 지운다)
import http from 'k6/http';
import exec from 'k6/execution';
import {Counter} from 'k6/metrics';
import {issueLimitedCoupon} from './lib/api.js';

const COUPON_SCHEME_ID = Number(__ENV.COUPON_SCHEME_ID || 2);
const MAX_ISSUE_COUNT = Number(__ENV.MAX_ISSUE_COUNT || 50);
const REQUESTS = Number(__ENV.REQUESTS || 100);
const VUS = Number(__ENV.VUS || Math.min(REQUESTS, 100));

const couponAccepted = new Counter('coupon_accepted');
const couponSoldOut = new Counter('coupon_sold_out');
const couponAlreadyIssued = new Counter('coupon_already_issued');
const couponUnexpected = new Counter('coupon_unexpected');

// 409 는 의도된 응답이므로 http_req_failed 에 실패로 집계하지 않는다
http.setResponseCallback(http.expectedStatuses(202, 409));

export const options = {
    scenarios: {
        limited_coupon_issues: {
            executor: 'shared-iterations',
            vus: VUS,
            iterations: REQUESTS,
            maxDuration: '5m',
        },
    },
    thresholds: {
        coupon_accepted: [`count==${Math.min(MAX_ISSUE_COUNT, REQUESTS)}`],
        coupon_sold_out: [`count==${Math.max(REQUESTS - MAX_ISSUE_COUNT, 0)}`],
        coupon_already_issued: ['count==0'],
        coupon_unexpected: ['count==0'],
        http_req_failed: ['rate==0'],
    },
};

export default function () {
    // 요청마다 다른 사용자 (1 ~ REQUESTS) 로 보내 중복 발급(409)과 섞이지 않게 한다
    const userId = exec.scenario.iterationInTest + 1;
    const res = issueLimitedCoupon(COUPON_SCHEME_ID, userId);

    // 409 는 매진과 중복 발급 둘 다 쓰므로 code 로 구분한다
    const code = res.status === 409 ? res.json('code') : null;
    if (res.status === 202) {
        couponAccepted.add(1);
    } else if (code === 'COUPON_SOLD_OUT') {
        couponSoldOut.add(1);
    } else if (code === 'COUPON_ALREADY_ISSUED') {
        couponAlreadyIssued.add(1);
    } else {
        couponUnexpected.add(1);
        console.error(`unexpected userId=${userId} status=${res.status} body=${res.body}`);
    }
}
