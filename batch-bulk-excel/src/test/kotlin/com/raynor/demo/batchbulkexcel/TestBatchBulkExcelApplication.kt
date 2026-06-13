package com.raynor.demo.batchbulkexcel

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<BatchBulkExcelApplication>().with(TestcontainersConfiguration::class).run(*args)
}
