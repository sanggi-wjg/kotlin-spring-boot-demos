package com.raynor.demo.boiler.service.coupon

import com.raynor.demo.boiler.infra.redis.lock.DistributedLockExecutor
import com.raynor.demo.boiler.service.coupon.model.CouponModel
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class CouponFacadeService(
    private val lockExecutor: DistributedLockExecutor,
    private val couponService: CouponService,
    private val couponSchemeService: CouponSchemeService,
    private val limitedCouponIssueService: LimitedCouponIssueService,
) {
    @Retryable(
        includes = [ObjectOptimisticLockingFailureException::class],
        maxRetries = 3,
        delay = 100L,
    )
    fun issueCoupon(
        couponSchemeId: Int,
        userId: Int,
    ): CouponModel {
        val lockKey = "coupon-scheme:$couponSchemeId"
        return lockExecutor.execute(lockKey, 10L, action = {
            couponService.issueCoupon(couponSchemeId, userId)
        })
    }

    fun issueLimitedCoupon(
        couponSchemeId: Int,
        userId: Int,
    ) {
        val scheme = couponSchemeService.getCouponSchemeById(couponSchemeId)
        limitedCouponIssueService.issueLimitedCoupon(couponSchemeId, userId, scheme.maxIssueCount)
    }
}
