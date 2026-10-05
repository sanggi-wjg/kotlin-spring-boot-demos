package com.raynor.demo.boiler.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.boiler.domain.coupon.Coupon
import com.raynor.demo.boiler.domain.coupon.QCoupon
import com.raynor.demo.boiler.domain.coupon.QCouponScheme
import org.springframework.data.jpa.repository.JpaRepository

interface CouponRepository :
    JpaRepository<Coupon, Long>,
    CouponQueryDslRepository {
    fun existsByUserIdAndCouponSchemeId(
        userId: Int,
        couponSchemeId: Int,
    ): Boolean
}

interface CouponQueryDslRepository {
    fun findAllByUserAndCursor(
        size: Long,
        cursorId: Long?,
        userId: Int,
    ): List<Coupon>
}

class CouponQueryDslRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : CouponQueryDslRepository {
    private val coupon = QCoupon.coupon
    private val couponScheme = QCouponScheme.couponScheme

    override fun findAllByUserAndCursor(
        size: Long,
        cursorId: Long?,
        userId: Int,
    ): List<Coupon> {
        return jpaQueryFactory
            .selectFrom(coupon)
            .join(coupon.couponScheme, couponScheme).fetchJoin()
            .where(
                coupon.user.id.eq(userId),
                cursorId?.let { coupon.id.lt(it) },
            )
            .orderBy(coupon.id.desc())
            .limit(size)
            .fetch()
    }
}
