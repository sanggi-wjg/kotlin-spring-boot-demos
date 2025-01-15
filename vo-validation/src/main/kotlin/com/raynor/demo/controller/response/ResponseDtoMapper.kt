package com.raynor.demo.controller.response

import com.raynor.demo.storage.entity.UserEntity

fun UserEntity.toUserResponseDto() = UserResponseDto(
    id = this.id,
    email = this.email,
    name = this.name,
    age = this.age
)
