package com.raynor.demo.boiler.service.coupon

import com.raynor.demo.boiler.domain.coupon.Coupon
import com.raynor.demo.boiler.repository.CouponRepository
import com.raynor.demo.boiler.repository.CouponSchemeRepository
import com.raynor.demo.boiler.repository.UserRepository
import com.raynor.demo.boiler.service.coupon.model.CouponModel
import com.raynor.demo.boiler.service.support.CursorSlice
import com.raynor.demo.boiler.shared.exception.AlreadyIssuedCouponException
import com.raynor.demo.boiler.shared.exception.NotIssuableCouponException
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponService(
    private val couponSchemeRepository: CouponSchemeRepository,
    private val couponRepository: CouponRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun issueCoupon(
        couponSchemeId: Int,
        userId: Int,
    ): CouponModel {
        val couponScheme = couponSchemeRepository.findByIdAndDeletedAtIsNull(couponSchemeId)
            ?: throw EntityNotFoundException("쿠폰 스키마를 찾을 수 없습니다: $couponSchemeId")
        val user = userRepository.findByIdOrNull(userId)
            ?: throw EntityNotFoundException("유저를 찾을 수 없습니다: $userId")

        if (!couponScheme.isIssuable()) {
            throw NotIssuableCouponException()
        }
        if (couponRepository.existsByUserIdAndCouponSchemeId(userId = userId, couponSchemeId = couponSchemeId)) {
            throw AlreadyIssuedCouponException()
        }

        couponScheme.increaseCurrentIssueCount()
        return couponRepository.save(
            Coupon(
                couponScheme = couponScheme,
                user = user,
            ),
        ).let {
            CouponModel.fromEntity(it)
        }
    }

    @Transactional(readOnly = true)
    fun getUserCoupons(
        size: Int,
        cursor: Long?,
        userId: Int,
    ): CursorSlice<Long, CouponModel> {
        val coupons = couponRepository.findAllByUserAndCursor(
            size = size.toLong() + 1,
            cursorId = cursor,
            userId = userId,
        )
        val items = coupons.take(size).map { CouponModel.fromEntity(it) }
        return CursorSlice(
            hasNext = coupons.size > size,
            nextCursor = items.lastOrNull()?.id,
            items = items,
        )
    }
}
