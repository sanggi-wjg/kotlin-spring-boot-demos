package com.raynor.demo.controller.request

import com.raynor.demo.domain.types.PositiveOrZeroInt
import com.raynor.demo.domain.types.RawPassword
import com.raynor.demo.domain.types.UserEmail
import com.raynor.demo.domain.types.UserName

data class UserCreationRequestDto(
    val email: String,
    val name: String,
    val age: Int,
    val password: String,
)

data class UserCreationRequest(
    val email: UserEmail,
    val name: UserName,
    val age: PositiveOrZeroInt,
    val password: RawPassword,
)
