package com.raynor.demo.aboutcoroutine.stub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StubServerApplication

fun main(args: Array<String>) {
    runApplication<StubServerApplication>(*args)
}
