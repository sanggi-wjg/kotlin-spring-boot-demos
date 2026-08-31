package com.raynor.demo.boiler.domain.coupon

import com.raynor.demo.boiler.domain.order.Order
import com.raynor.demo.boiler.domain.support.BaseEntity
import com.raynor.demo.boiler.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "coupon",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_coupon_001", columnNames = ["coupon_scheme_id", "user_id"]),
    ],
)
open class Coupon(
    couponScheme: CouponScheme,
    user: User? = null,
    startedAt: LocalDateTime,
    expiredAt: LocalDateTime,
    usedAt: LocalDateTime? = null,
    order: Order? = null,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_scheme_id", nullable = false)
    var couponScheme: CouponScheme = couponScheme
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = user
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime = startedAt
        protected set

    @Column(name = "expired_at", nullable = false)
    var expiredAt: LocalDateTime = expiredAt
        protected set

    @Column(name = "used_at")
    var usedAt: LocalDateTime? = usedAt
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order? = order
        protected set
}
