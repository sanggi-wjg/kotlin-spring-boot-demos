package com.raynor.demo.springbatchargoworkflows

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration

@ConfigurationPropertiesScan(basePackages = ["com.raynor.demo.springbatchargoworkflows"])
@SpringBootApplication(scanBasePackages = ["com.raynor.demo.springbatchargoworkflows"])
class SpringBatchArgoWorkflowsApplication

fun main(args: Array<String>) {
    runApplication<SpringBatchArgoWorkflowsApplication>(*args)
}
