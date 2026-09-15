package com.raynor.demo.boiler.domain.support

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.math.BigDecimal
import java.math.RoundingMode

@Embeddable
open class Money(
    amount: BigDecimal,
) {
    companion object {
        private const val SCALE = 0
        private val ROUNDING_MODE = RoundingMode.DOWN

        val ZERO = Money(BigDecimal.ZERO)
    }

    init {
        require(amount >= BigDecimal.ZERO) { "amount must be greater than or equal to zero" }
    }

    @Column(name = "amount", nullable = false, precision = 15, scale = 0)
    val amount: BigDecimal = amount.setScale(SCALE, ROUNDING_MODE)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Money) return false
        return amount.compareTo(other.amount) == 0
    }

    override fun hashCode(): Int = amount.stripTrailingZeros().hashCode()

    override fun toString(): String = amount.toPlainString()

    operator fun plus(other: Money): Money = Money(amount + other.amount)

    operator fun minus(other: Money): Money = Money(amount - other.amount)

    operator fun times(other: Long): Money = Money(amount * BigDecimal(other.toString()))

    operator fun times(other: Int): Money = Money(amount * BigDecimal(other.toString()))
}

fun Int.toMoney() = Money(BigDecimal(this.toString()))

fun String.toMoney() = Money(BigDecimal(this))

fun BigDecimal.toMoney() = Money(this)

// fun Iterable<Money>.sumOf(selector: (Money) -> Int): Int = sumOf(selector)
