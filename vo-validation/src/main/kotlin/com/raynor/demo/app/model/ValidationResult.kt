package com.raynor.demo.app.model

data class ValidationResult(
    private val _errors: MutableList<FieldValidation> = mutableListOf()
) {
    data class FieldValidation(
        val field: String,
        val message: String
    )

    val errors get() = _errors.toList()

    val isValid: Boolean
        get() = errors.isEmpty()

    val hasError: Boolean
        get() = errors.isNotEmpty()

    fun addError(field: String, message: String) {
        _errors.add(
            FieldValidation(field, message)
        )
    }

    fun throwIfError() {
        if (hasError) {
            throw IllegalArgumentException("Invalid request: $this")
        }
    }
}
