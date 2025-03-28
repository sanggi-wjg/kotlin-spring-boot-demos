package com.raynor.demo.transactionaloutbox.model

import java.time.Instant

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
