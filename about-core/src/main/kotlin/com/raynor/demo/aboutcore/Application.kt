package com.raynor.demo.aboutcore

import com.raynor.demo.aboutcore.product.ProductRepository
import com.raynor.demo.aboutcore.product.ProductSearchService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    val application = runApplication<Application>(*args)

    val context = AnnotationConfigApplicationContext(ProductSearchService::class.java, ProductRepository::class.java)
    val service = context.getBean("productSearchService")
}
