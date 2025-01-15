package com.raynor.demo.domain.validator

import com.raynor.demo.domain.model.ValidationResult
import com.raynor.demo.domain.types.PositiveOrZeroInt
import com.raynor.demo.domain.types.UserEmail
import com.raynor.demo.domain.types.UserId
import com.raynor.demo.domain.types.UserName
import kotlin.reflect.full.declaredMemberProperties

object ModelValidator {

    fun validateModel(model: Any): ValidationResult {
        val result = ValidationResult()
        model::class.declaredMemberProperties.forEach { property ->
            validateFieldOfCustomTyped(property.name, property.getter.call(model), result)
        }
        return result
    }

    fun validateTyped(type: Any): ValidationResult {
        val result = ValidationResult()
        validateFieldOfCustomTyped(type::class.java.simpleName, type, result)
        return result
    }

    private fun validateFieldOfCustomTyped(fieldName: String, field: Any?, result: ValidationResult) {
        when (field) {
            is PositiveOrZeroInt -> {
                if (!field.validate()) {
                    result.addError(fieldName, "${PositiveOrZeroInt.INVALID_MESSAGE}: ${field.value}")
                }
            }

            is UserEmail -> {
                if (!field.validate()) {
                    result.addError(fieldName, "${UserEmail.INVALID_MESSAGE}: ${field.value}")
                }
            }

            is UserName -> {
                if (!field.validate()) {
                    result.addError(fieldName, "${UserName.INVALID_MESSAGE}: ${field.value}")
                }
            }

            is UserId -> {
                if (!field.validate()) {
                    result.addError(fieldName, "${UserId.INVALID_MESSAGE}: ${field.value}")
                }
            }
        }
    }
}

fun <T> validateModelOf(function: () -> T): T {
    return function.invoke().also {
        ModelValidator.validateModel(it as Any).throwIfError()
    }
}

fun <T> validateTypedOf(function: () -> T): T {
    return function.invoke().also {
        ModelValidator.validateTyped(it as Any).throwIfError()
    }
}
