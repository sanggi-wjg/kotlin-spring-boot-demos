package com.raynor.demo.shared.typed

import java.math.BigDecimal

@JvmInline
value class Money(val value: BigDecimal) {
    init {
        require(value >= BigDecimal.ZERO) { "금액은 0보다 작을 수 없습니다." }
    }

    operator fun plus(other: Money): Money {
        return Money(this.value + other.value)
    }

    operator fun minus(other: Money): Money {
        return Money(this.value - other.value)
    }
}

fun BigDecimal.toMoney(): Money {
    return Money(this)
}