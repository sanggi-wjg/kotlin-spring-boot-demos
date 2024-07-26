package com.raynor.demo.app.types

data class UserEmail(val value: String) {

    fun validate(): Boolean {
        return innerValidate(this.value)
    }

    companion object {
        private val USER_EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        const val INVALID_MESSAGE = "UserEmail must has valid format"

        private fun innerValidate(value: String): Boolean {
            return USER_EMAIL_REGEX.matches(value)
        }
    }
}
