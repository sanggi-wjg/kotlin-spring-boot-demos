package com.raynor.demo.redis.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("person")
data class Person(
    @Id val id: Int,
    val name: String,
    val age: Int,
    val address: Address,
)

data class Address(
    val street: String,
    val city: String,
)
