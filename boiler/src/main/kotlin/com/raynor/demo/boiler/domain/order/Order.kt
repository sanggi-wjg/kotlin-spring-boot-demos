package com.raynor.demo.boiler.domain.order

import com.raynor.demo.boiler.domain.support.BaseEntity
import com.raynor.demo.boiler.domain.support.Money
import com.raynor.demo.boiler.domain.user.User
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "`order`")
open class Order(
    user: User,
    status: OrderStatus,
    amount: Money,
    couponDiscountAmount: Money,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = user
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 64)
    var status: OrderStatus = status
        protected set

    @Embedded
    @AttributeOverride(name = "amount", column = Column(name = "amount", nullable = false, precision = 15, scale = 0))
    var amount: Money = amount
        protected set

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "coupon_discount_amount", nullable = false, precision = 15, scale = 0),
    )
    var couponDiscountAmount: Money = couponDiscountAmount
        protected set
}
