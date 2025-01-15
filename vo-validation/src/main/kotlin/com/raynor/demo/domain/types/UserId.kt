package com.raynor.demo.domain.types

data class UserId(val value: Int) {

    fun validate(): Boolean {
        return innerValidate(this.value)
    }

    companion object {
        const val INVALID_MESSAGE = "UserId must be positive"

        fun innerValidate(value: Int): Boolean {
            return value > 0
        }
    }
}
