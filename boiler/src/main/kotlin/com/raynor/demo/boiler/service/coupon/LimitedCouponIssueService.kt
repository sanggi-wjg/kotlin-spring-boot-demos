package com.raynor.demo.boiler.service.coupon

import org.redisson.api.RScript
import org.redisson.api.RedissonClient
import org.redisson.client.codec.StringCodec
import org.springframework.stereotype.Service

@Service
class LimitedCouponIssueService(
    private val redissonClient: RedissonClient,
) {
    fun issueLimitedCoupon(
        couponSchemeId: Int,
        userId: Int,
        limit: Int,
    ) {
        val key = "limited-coupon-issue:$couponSchemeId"
        val result = redissonClient.getScript(StringCodec()).eval<Long>(
            RScript.Mode.READ_WRITE,
            """
            local key = KEYS[1]
            local userId = ARGV[1]
            local limit = tonumber(ARGV[2])

            -- 1. 이미 발급받은 유저
            if redis.call("SISMEMBER", key, userId) == 1 then
                return 0
            end

            -- 2. 한도 도달
            if redis.call("SCARD", key) >= limit then
                return -1
            end

            -- 3. 발급
            redis.call("SADD", key, userId)
            return 1
            """.trimIndent(),
            RScript.ReturnType.LONG,
            listOf(key),
            userId,
            limit,
        )

        if (result == 0L) {
            TODO("이미 발급")
        } else if (result == -1L) {
            TODO("쿠폰 한도 초과")
        }

        TODO("발급 produce")
    }
}
