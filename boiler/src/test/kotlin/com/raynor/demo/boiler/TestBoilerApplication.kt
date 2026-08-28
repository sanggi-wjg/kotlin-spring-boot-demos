package com.raynor.demo.boiler

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<BoilerApplication>().with(TestcontainersConfiguration::class).run(*args)
}
