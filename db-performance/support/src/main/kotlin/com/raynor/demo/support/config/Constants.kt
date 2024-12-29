package com.raynor.demo.support.config

object Constants {
    const val BATCH_SIZE = 1_000
    const val CYCLE_COUNT = 100
    const val INSERT_SIZE = 1_000
    const val SELECT_RANGE = INSERT_SIZE / CYCLE_COUNT
    const val OFFSET = 100
}