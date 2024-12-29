package com.raynor.demo.support.config

object Constants {
    const val TEST_CYCLE_COUNT = 20
    const val DB_REPEAT_COUNT = 2_000
    const val DB_REPEAT_COUNT_PER_CYCLE = DB_REPEAT_COUNT / TEST_CYCLE_COUNT
    const val DB_BATCH_SIZE = 1_000
}