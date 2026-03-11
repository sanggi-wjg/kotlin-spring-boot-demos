package com.raynor.demo.k8sqos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class K8sQosApplication

fun main(args: Array<String>) {
    runApplication<K8sQosApplication>(*args)
}
