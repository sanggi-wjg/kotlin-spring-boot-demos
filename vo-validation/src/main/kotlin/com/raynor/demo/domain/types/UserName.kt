package com.raynor.demo.domain.types

data class UserName(val value: String) {

    fun validate(): Boolean {
        return innerValidate(this.value)
    }

    companion object {
        private val USER_NAME_REGEX = "^[A-Za-z]{2,}$".toRegex()
        const val INVALID_MESSAGE = "UserName must has valid format"

        fun innerValidate(value: String): Boolean {
            return USER_NAME_REGEX.matches(value)
        }
    }

    override fun toString(): String {
        return "UserName(value=<hidden>)"
    }
}
