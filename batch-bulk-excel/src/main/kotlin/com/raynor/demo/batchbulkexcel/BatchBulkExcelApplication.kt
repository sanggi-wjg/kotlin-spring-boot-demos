package com.raynor.demo.batchbulkexcel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication(
    scanBasePackages = ["com.raynor.demo.batchbulkexcel"],
)
class BatchBulkExcelApplication

fun main(args: Array<String>) {
    runApplication<BatchBulkExcelApplication>(*args)
}
