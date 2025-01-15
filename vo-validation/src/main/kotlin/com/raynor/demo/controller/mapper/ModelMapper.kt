package com.raynor.demo.controller.mapper

import com.raynor.demo.controller.request.UserCreationRequest
import com.raynor.demo.controller.request.UserCreationRequestDto
import com.raynor.demo.controller.response.UserResponseDto
import com.raynor.demo.domain.types.PositiveOrZeroInt
import com.raynor.demo.domain.types.UserEmail
import com.raynor.demo.domain.types.UserId
import com.raynor.demo.domain.types.UserName
import com.raynor.demo.domain.validator.validateModelOf
import com.raynor.demo.domain.validator.validateTypedOf
import com.raynor.demo.storage.entity.UserEntity

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

fun UserEntity.toUserResponseDto() = UserResponseDto(
    id = this.id,
    email = this.email,
    name = this.name,
    age = this.age
)
