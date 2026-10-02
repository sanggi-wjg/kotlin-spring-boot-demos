package com.raynor.demo.aboutcoroutine.basics.part0

import java.time.Duration
import java.util.logging.Logger
import kotlin.concurrent.thread
import kotlin.time.measureTime

private val logger: Logger = Logger.getLogger("part-0-1")

fun main() {
    logger.info("PID: ${ProcessHandle.current().pid()}")
    println("Press Enter to continue...")
    readln()

    val result = measureTime {
        repeat(1000) { index ->
            thread {
                Thread.sleep(Duration.ofSeconds(10))
            }
        }
    }
    logger.info("Time elapsed: $result")
}
