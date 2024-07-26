package com.raynor.demo.app.dto

import com.raynor.demo.app.types.PositiveOrZeroInt
import com.raynor.demo.app.types.UserEmail
import com.raynor.demo.app.types.UserId
import com.raynor.demo.app.types.UserName
import com.raynor.demo.app.validator.validateModelOf
import com.raynor.demo.app.validator.validateTypedOf

fun UserCreationRequestDto.toModel() = validateModelOf {
    UserCreationRequest(
        email = UserEmail(this.email),
        name = UserName(this.name),
        age = PositiveOrZeroInt(this.age)
    )
}

fun Int.toUserId() = validateTypedOf {
    UserId(this)
}
