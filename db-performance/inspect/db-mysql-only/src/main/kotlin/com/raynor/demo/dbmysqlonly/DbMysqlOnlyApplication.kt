package com.raynor.demo.dbmysqlonly

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.raynor.demo"])
class DbMysqlOnlyApplication

fun main(args: Array<String>) {
    runApplication<DbMysqlOnlyApplication>(*args)
}
