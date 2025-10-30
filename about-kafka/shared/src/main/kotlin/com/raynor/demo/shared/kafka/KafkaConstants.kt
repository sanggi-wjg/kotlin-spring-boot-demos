package com.raynor.demo.shared.kafka

object KafkaConstants {
    const val RETRY_TOPIC_SUFFIX = "-retry"
    const val DLT_SUFFIX = "-dlt"
    const val CONCURRENCY = 3

    object Retry {
        const val MAX_ATTEMPTS = 5
        const val BACKOFF_MULTIPLIER = 2.0
        const val BACKOFF_INITIAL_DELAY = 2000L
        const val BACKOFF_MAX_DELAY = 60000L
    }
}