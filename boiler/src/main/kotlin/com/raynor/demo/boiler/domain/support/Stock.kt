package com.raynor.demo.boiler.domain.support

import com.raynor.demo.boiler.domain.product.StockStatus
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
open class Stock(
    @Column(name = "stock_quantity", nullable = false)
    val quantity: Long,
) {
    init {
        require(quantity >= 0) { "quantity must be greater than or equal to zero" }
    }

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

    fun increase(amount: Long): Stock {
        require(amount >= 0) { "increase amount must be greater than or equal to zero" }

        return Stock(quantity + amount)
    }

    fun decrease(amount: Long): Stock {
        require(amount >= 0) { "decrease amount must be greater than or equal to zero" }
        check(quantity >= amount) { "stock is not enough. current=$quantity, requested=$amount" }

        return Stock(quantity - amount)
    }
}

fun Long.toStock() = Stock(this)
