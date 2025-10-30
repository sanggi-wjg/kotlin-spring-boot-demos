package com.raynor.demo.shared.kafka.message

import java.time.Instant

data class FirstScenarioEventMessage(
    val eventId: String,
    val message: String,
    val randomValue: Int,
    val timestamp: Instant,
)