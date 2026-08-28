package com.raynor.demo.boiler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan("com.raynor.demo.boiler")
@SpringBootApplication(scanBasePackages = ["com.raynor.demo.boiler"])
class BoilerApplication

fun main(args: Array<String>) {
    runApplication<BoilerApplication>(*args)
}
