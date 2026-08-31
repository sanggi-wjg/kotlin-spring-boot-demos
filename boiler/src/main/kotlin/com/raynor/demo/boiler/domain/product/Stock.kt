package com.raynor.demo.boiler.domain.product

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
open class Stock(
    quantity: Long,
) {
    init {
        require(quantity >= 0) { "quantity must be greater than or equal to zero" }
    }

    @Column(name = "stock_quantity", nullable = false)
    var quantity: Long = quantity
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Stock) return false
        return quantity == other.quantity
    }

    override fun hashCode(): Int = quantity.hashCode()

    override fun toString(): String = quantity.toString()

    fun getStatus(): StockStatus {
        return if (quantity > 0) {
            StockStatus.IN_STOCK
        } else {
            StockStatus.OUT_OF_STOCK
        }
    }

    fun increase(amount: Long) {
        check(amount >= 0) { "quantity must be greater than or equal to zero" }

        this.quantity += amount
    }

    fun decrease(amount: Long) {
        check(amount >= 0) { "quantity must be greater than or equal to zero" }
        check(quantity >= amount) { "quantity must be greater than or equal to $amount" }

        this.quantity -= amount
    }
}

fun Long.toStock() = Stock(this)
