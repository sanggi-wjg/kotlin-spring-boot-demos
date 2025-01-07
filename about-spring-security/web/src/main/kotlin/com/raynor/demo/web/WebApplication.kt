package com.raynor.demo.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan("com.raynor.demo")
@SpringBootApplication(scanBasePackages = ["com.raynor.demo"])
class WebApplication

fun main(args: Array<String>) {
    runApplication<WebApplication>(*args)
}
