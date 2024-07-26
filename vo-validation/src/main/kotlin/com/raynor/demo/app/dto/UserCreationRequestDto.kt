package com.raynor.demo.app.dto

import com.raynor.demo.app.types.PositiveOrZeroInt
import com.raynor.demo.app.types.UserEmail
import com.raynor.demo.app.types.UserName

data class UserCreationRequestDto(
    val email: String,
    val name: String,
    val age: Int
)

data class UserCreationRequest(
    val email: UserEmail,
    val name: UserName,
    val age: PositiveOrZeroInt,
)
