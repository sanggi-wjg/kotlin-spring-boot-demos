package com.raynor.demo.core.exception

import com.raynor.demo.app.model.ValidationResult

class InvalidRequestException : RuntimeException {
    var validationErrors: List<ValidationResult.FieldValidation> = emptyList()
        private set

    constructor(validationErrors: List<ValidationResult.FieldValidation>) : super() {
        this.validationErrors = validationErrors
    }
}