package com.raynor.demo.productservice.message.config

object KafkaConstants {
    const val RETRY_TOPIC_SUFFIX = "-retry"
    const val DLT_SUFFIX = "-dlt"
    const val CONCURRENCY = 3

    object Retry {
        const val MAX_ATTEMPTS = 3
        const val BACKOFF_MULTIPLIER = 2.0
        const val BACKOFF_INITIAL_DELAY = 1000L
        const val BACKOFF_MAX_DELAY = 10000L
    }
}