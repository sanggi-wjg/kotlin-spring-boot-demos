package com.raynor.demo.aboutcore

import com.raynor.demo.aboutcore.service.ProductService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)

    val context = AnnotationConfigApplicationContext(ProductService::class.java)
    val service = context.getBean("productService")
    println(service)
}
