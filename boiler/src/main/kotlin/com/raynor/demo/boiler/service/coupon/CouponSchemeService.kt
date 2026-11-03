package com.raynor.demo.boiler.service.coupon

import com.raynor.demo.boiler.domain.coupon.CouponScheme
import com.raynor.demo.boiler.domain.support.toMoney
import com.raynor.demo.boiler.infra.redis.config.CacheKey
import com.raynor.demo.boiler.repository.CouponSchemeRepository
import com.raynor.demo.boiler.service.coupon.model.CouponSchemeCreateRequest
import com.raynor.demo.boiler.service.coupon.model.CouponSchemeModel
import jakarta.persistence.EntityNotFoundException
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponSchemeService(
    private val couponSchemeRepository: CouponSchemeRepository,
) {

    @Cacheable(value = [CacheKey.COUPON_SCHEME], key = "#couponSchemeId", sync = true)
    @Transactional(readOnly = true)
    fun getCouponSchemeById(couponSchemeId: Int): CouponSchemeModel {
        val scheme = couponSchemeRepository.findByIdOrNull(couponSchemeId)
            ?: throw EntityNotFoundException("찾을수 없는 쿠폰 스킴")
        return CouponSchemeModel.fromEntity(scheme)
    }

    @CachePut(value = [CacheKey.COUPON_SCHEME], key = "#result.id")
    @Transactional
    fun createCouponScheme(request: CouponSchemeCreateRequest): CouponSchemeModel {
        val couponScheme = CouponScheme(
            name = request.name,
            discountType = request.discountType,
            discountAmount = request.discountAmount?.toMoney(),
            discountRate = request.discountRate,
            maxDiscountAmount = request.maxDiscountAmount?.toMoney(),
            minOrderAmount = request.minOrderAmount.toMoney(),
            usingStartedAt = request.usingStartedAt,
            usingExpiredAt = request.usingEndedAt,
            maxIssueCount = request.maxIssueCount,
        )
        return couponSchemeRepository.save(couponScheme).let {
            CouponSchemeModel.fromEntity(it)
        }
    }
}
