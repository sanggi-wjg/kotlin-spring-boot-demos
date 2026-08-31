package com.raynor.demo.boiler.domain.order

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

@Entity
@Table(name = "`order`")
open class Order(
    status: OrderStatus,
    amount: BigDecimal,
    couponDiscountAmount: BigDecimal,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 64)
    var status: OrderStatus = status
        protected set

    @Column(name = "amount", nullable = false, precision = 15, scale = 0)
    var amount: BigDecimal = amount
        protected set

    @Column(name = "coupon_discount_amount", nullable = false, precision = 15, scale = 0)
    var couponDiscountAmount: BigDecimal = couponDiscountAmount
        protected set
}
