package com.raynor.demo.app.validator

import com.raynor.demo.app.model.ValidationResult
import com.raynor.demo.app.types.PositiveOrZeroInt
import com.raynor.demo.app.types.UserEmail
import com.raynor.demo.app.types.UserName
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

object ModelValidator {

    fun validate(model: Any): ValidationResult {
        val result = ValidationResult()

        model::class.declaredMemberProperties.forEach { property ->
            val fieldName = property.name
            val fieldValue = property.getter.call(model)

            when (property.returnType) {
                PositiveOrZeroInt::class.createType() -> {
                    if (!(fieldValue as PositiveOrZeroInt).validate()) {
                        result.addError(fieldName, "${PositiveOrZeroInt.INVALID_MESSAGE}: $fieldValue")
                    }
                }

                UserEmail::class.createType() -> {
                    if (!(fieldValue as UserEmail).validate()) {
                        result.addError(fieldName, "${UserEmail.INVALID_MESSAGE}: $fieldValue")
                    }
                }

                UserName::class.createType() -> {
                    if (!(fieldValue as UserName).validate()) {
                        result.addError(fieldName, "${UserName.INVALID_MESSAGE}: $fieldValue")
                    }
                }
            }
        }

        return result
    }
}
