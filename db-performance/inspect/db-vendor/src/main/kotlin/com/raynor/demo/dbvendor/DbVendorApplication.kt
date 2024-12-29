package com.raynor.demo.dbvendor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.raynor.demo"])
@ConfigurationPropertiesScan("com.raynor.demo")
class DbVendorApplication

fun main(args: Array<String>) {
    runApplication<DbVendorApplication>(*args)
}
