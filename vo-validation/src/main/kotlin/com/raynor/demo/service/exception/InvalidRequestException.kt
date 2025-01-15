package com.raynor.demo.service.exception

import com.raynor.demo.domain.validator.model.ValidationResult

class InvalidRequestException(
    validationErrors: List<ValidationResult.FieldValidation>
) : RuntimeException() {
    var validationErrors: List<ValidationResult.FieldValidation> = validationErrors
        private set
}
