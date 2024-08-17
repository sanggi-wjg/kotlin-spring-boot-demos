package com.raynor.demo.transactionaloutbox.consumer.model

data class UserSignedEvent(
    val name: String,
    val email: String,
)
