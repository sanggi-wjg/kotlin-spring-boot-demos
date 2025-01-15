package com.raynor.demo.domain.types

data class RawPassword(val value: String) {

    fun validate(): Boolean {
        return innerValidate(this.value)
    }

    companion object {
        const val INVALID_MESSAGE = "Password must be greater than 10 characters"

        fun innerValidate(value: String): Boolean {
            return value.length >= 10
        }
    }
}