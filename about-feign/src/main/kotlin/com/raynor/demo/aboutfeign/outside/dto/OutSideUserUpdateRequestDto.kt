package com.raynor.demo.aboutfeign.outside.dto

data class OutSideUserUpdateRequestDto(
    val name: String? = null,
    val status: OutSideUserStatus? = null
)
