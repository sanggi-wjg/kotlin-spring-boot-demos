package com.raynor.demo.boiler.domain.coupon

import com.raynor.demo.boiler.domain.support.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "coupon_scheme")
open class CouponScheme(
    discountType: DiscountType,
    discountAmount: BigDecimal? = null,
    discountRate: BigDecimal? = null,
    maxDiscountAmount: BigDecimal? = null,
    minOrderAmount: BigDecimal = BigDecimal.ZERO,
    usingStartedAt: LocalDateTime,
    usingExpiredAt: LocalDateTime,
    maxIssueCount: Int,
    currentIssueCount: Int = 0,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 64)
    var discountType: DiscountType = discountType
        protected set

    @Column(name = "discount_amount", precision = 15, scale = 0)
    var discountAmount: BigDecimal? = discountAmount
        protected set

    @Column(name = "discount_rate", precision = 5, scale = 2)
    var discountRate: BigDecimal? = discountRate
        protected set

    @Column(name = "max_discount_amount", precision = 15, scale = 0)
    var maxDiscountAmount: BigDecimal? = maxDiscountAmount
        protected set

    @Column(name = "min_order_amount", nullable = false, precision = 15, scale = 0)
    var minOrderAmount: BigDecimal = minOrderAmount
        protected set

    @Column(name = "using_started_at", nullable = false)
    var usingStartedAt: LocalDateTime = usingStartedAt
        protected set

    @Column(name = "using_expired_at", nullable = false)
    var usingExpiredAt: LocalDateTime = usingExpiredAt
        protected set

    @Column(name = "max_issue_count", nullable = false)
    var maxIssueCount: Int = maxIssueCount
        protected set

    @Column(name = "current_issue_count", nullable = false)
    var currentIssueCount: Int = currentIssueCount
        protected set
}
