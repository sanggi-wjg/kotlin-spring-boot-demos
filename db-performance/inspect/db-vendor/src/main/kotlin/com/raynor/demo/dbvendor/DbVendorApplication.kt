package com.raynor.demo.dbvendor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("com.raynor.demo.dbvendor")
class DbVendorApplication

fun main(args: Array<String>) {
    runApplication<DbVendorApplication>(*args)
}
