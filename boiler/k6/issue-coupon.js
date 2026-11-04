// 선착순 쿠폰 발급 (Redisson 분산 락 + DB) 수량 정합성·응답 시간 확인 (requirements.md F4: 동시 발급 요청이 수량을 초과하면 초과분만 실패)
// 최대 발급 수량 MAX_ISSUE_COUNT 인 쿠폰 스킴에 서로 다른 사용자 REQUESTS 명이 VUS 개의 VU 로 나눠 발급을 요청한다.
// 기대: 201 은 정확히 MAX_ISSUE_COUNT 건, 409(COUPON_NOT_ISSUABLE) 는 REQUESTS - MAX_ISSUE_COUNT 건, 그 외 응답 0건
// 요청이 몰려 락 획득에 실패하면 503 이 나며, coupon_lock_failed 로 따로 센다 (부하 비교 시 확인할 지표).
//
// 실행: k6 run -e COUPON_SCHEME_ID=1 -e MAX_ISSUE_COUNT=50 -e REQUESTS=10000 -e VUS=200 k6/issue-coupon.js
//   (사용자 id 1 ~ REQUESTS 가 DB 에 있어야 한다)
//
// 실행 전 초기화 (이전 발급 기록이 남아 있으면 전부 중복 발급 409 가 된다)
//   DELETE FROM coupon WHERE coupon_scheme_id = {id};
//   UPDATE coupon_scheme SET current_issue_count = 0 WHERE id = {id};
import http from 'k6/http';
import exec from 'k6/execution';
import {Counter} from 'k6/metrics';
import {issueCoupon, uniqueKey} from './lib/api.js';

const COUPON_SCHEME_ID = Number(__ENV.COUPON_SCHEME_ID || 1);
const MAX_ISSUE_COUNT = Number(__ENV.MAX_ISSUE_COUNT || 50);
const REQUESTS = Number(__ENV.REQUESTS || 100);
const VUS = Number(__ENV.VUS || Math.min(REQUESTS, 100));

const couponIssued = new Counter('coupon_issued');
const couponNotIssuable = new Counter('coupon_not_issuable');
const couponAlreadyIssued = new Counter('coupon_already_issued');
const couponLockFailed = new Counter('coupon_lock_failed');
const couponUnexpected = new Counter('coupon_unexpected');

// 409 는 의도된 응답이므로 http_req_failed 에 실패로 집계하지 않는다 (503 은 실패로 남긴다)
http.setResponseCallback(http.expectedStatuses(201, 409));

export const options = {
    scenarios: {
        concurrent_issues: {
            executor: 'shared-iterations',
            vus: VUS,
            iterations: REQUESTS,
            maxDuration: '5m',
        },
    },
    thresholds: {
        coupon_issued: [`count==${Math.min(MAX_ISSUE_COUNT, REQUESTS)}`],
        coupon_not_issuable: [`count==${Math.max(REQUESTS - MAX_ISSUE_COUNT, 0)}`],
        coupon_already_issued: ['count==0'],
        coupon_lock_failed: ['count==0'],
        coupon_unexpected: ['count==0'],
    },
};

export default function () {
    // 요청마다 다른 사용자 (1 ~ REQUESTS) 로 보내 중복 발급(409)과 섞이지 않게 한다
    const userId = exec.scenario.iterationInTest + 1;
    const res = issueCoupon(uniqueKey('issue-coupon', userId), COUPON_SCHEME_ID, userId);

    // 409 는 수량 소진과 중복 발급 둘 다 쓰므로 code 로 구분한다
    const code = res.status === 409 ? res.json('code') : null;
    if (res.status === 201) {
        couponIssued.add(1);
    } else if (code === 'COUPON_NOT_ISSUABLE') {
        couponNotIssuable.add(1);
    } else if (code === 'COUPON_ALREADY_ISSUED') {
        couponAlreadyIssued.add(1);
    } else if (res.status === 503) {
        couponLockFailed.add(1);
    } else {
        couponUnexpected.add(1);
        console.error(`unexpected userId=${userId} status=${res.status} body=${res.body}`);
    }
}
