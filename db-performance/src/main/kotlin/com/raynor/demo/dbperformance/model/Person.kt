package com.raynor.demo.dbperformance.model

import java.sql.Timestamp
import java.time.Instant

data class Person(
    val id: Int? = null,
    val name: String,
    val age: Int,
    val isActive: Boolean,
    val createdAt: Instant,
    val createdAtTimeStamp: Timestamp? = null,
)
