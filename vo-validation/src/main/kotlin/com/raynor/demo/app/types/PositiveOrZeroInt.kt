package com.raynor.demo.app.types

data class PositiveOrZeroInt(val value: Int) {

    fun validate(): Boolean {
        return innerValidate(this.value)
    }

    companion object {
        const val INVALID_MESSAGE = "PositiveOrZeroInt must be positive or zero"

        fun innerValidate(value: Int): Boolean {
            return value >= 0
        }
    }
}
