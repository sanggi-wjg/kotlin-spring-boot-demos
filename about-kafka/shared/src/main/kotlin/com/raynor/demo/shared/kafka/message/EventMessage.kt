package com.raynor.demo.shared.kafka.message

import java.time.Instant

data class EventMessage(
    val eventId: String,
    val message: String,
    val randomValue: Int,
    val timestamp: Instant,
) {
    fun isRandomValueDivisibleByTen(): Boolean {
        return randomValue % 10 == 0
    }
}