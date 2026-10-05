package com.raynor.demo.boiler.domain.coupon

import com.raynor.demo.boiler.domain.support.BaseEntity
import com.raynor.demo.boiler.domain.support.Money
import com.raynor.demo.boiler.shared.exception.NotIssuableCouponException
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant
import kotlin.math.max

@Entity
@Table(name = "coupon_scheme")
open class CouponScheme(
    name: String,
    discountType: DiscountType,
    discountAmount: Money? = null,
    discountRate: BigDecimal? = null,
    maxDiscountAmount: Money? = null,
    minOrderAmount: Money = Money(BigDecimal.ZERO),
    usingStartedAt: Instant,
    usingExpiredAt: Instant,
    maxIssueCount: Int,
    currentIssueCount: Int = 0,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        protected set

    @Column(name = "name", nullable = false, length = 64)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 64)
    var discountType: DiscountType = discountType
        protected set

    @Embedded
    @AttributeOverride(name = "amount", column = Column(name = "discount_amount", precision = 15, scale = 0))
    var discountAmount: Money? = discountAmount
        protected set

    // 할인율은 금액이 아니므로 Money 로 감싸지 않는다
    @Column(name = "discount_rate", precision = 5, scale = 2)
    var discountRate: BigDecimal? = discountRate
        protected set

    @Embedded
    @AttributeOverride(name = "amount", column = Column(name = "max_discount_amount", precision = 15, scale = 0))
    var maxDiscountAmount: Money? = maxDiscountAmount
        protected set

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "min_order_amount", nullable = false, precision = 15, scale = 0),
    )
    var minOrderAmount: Money = minOrderAmount
        protected set

    @Column(name = "using_started_at", nullable = false)
    var usingStartedAt: Instant = usingStartedAt
        protected set

    @Column(name = "using_expired_at", nullable = false)
    var usingExpiredAt: Instant = usingExpiredAt
        protected set

    @Column(name = "max_issue_count", nullable = false)
    var maxIssueCount: Int = maxIssueCount
        protected set

    @Column(name = "current_issue_count", nullable = false)
    var currentIssueCount: Int = currentIssueCount
        protected set

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0
        protected set

    fun isIssuable(): Boolean {
        val now = Instant.now()
        return (now in usingStartedAt..<usingExpiredAt) &&
            (currentIssueCount < maxIssueCount)
    }

    fun increaseCurrentIssueCount() {
        if (currentIssueCount >= maxIssueCount) throw NotIssuableCouponException()

        currentIssueCount++
    }
}
