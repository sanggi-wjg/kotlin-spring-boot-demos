package com.raynor.demo.controller.request

import com.raynor.demo.domain.types.*
import com.raynor.demo.domain.validator.validateModelOf
import com.raynor.demo.domain.validator.validateTypedOf

fun UserCreationRequestDto.toModel() = validateModelOf {
    UserCreationRequest(
        email = UserEmail(this.email),
        name = UserName(this.name),
        age = PositiveOrZeroInt(this.age),
        password = RawPassword(this.password)
    )
}

fun Int.toUserId() = validateTypedOf {
    UserId(this)
}
