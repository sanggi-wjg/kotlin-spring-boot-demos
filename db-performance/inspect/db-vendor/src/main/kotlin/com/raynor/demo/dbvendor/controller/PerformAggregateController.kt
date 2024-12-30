package com.raynor.demo.dbvendor.controller

import com.raynor.demo.dbvendor.service.MongoDBPerformanceService
import com.raynor.demo.dbvendor.service.MySQLPerformanceService
import com.raynor.demo.dbvendor.service.PerformanceService
import com.raynor.demo.dbvendor.service.PostgreSQLPerformanceService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/aggregate")
class PerformAggregateController(
    @Qualifier(MySQLPerformanceService.BEAN_NAME) private val mysqlService: PerformanceService,
    @Qualifier(PostgreSQLPerformanceService.BEAN_NAME) private val postgresqlService: PerformanceService,
    @Qualifier(MongoDBPerformanceService.BEAN_NAME) private val mongoDBService: PerformanceService,
) {

    @GetMapping("/create")
    fun cretate() {
        mysqlService.create()
        postgresqlService.create()
        mongoDBService.create()
    }

    @GetMapping("/update")
    fun update() {
        mysqlService.update()
        postgresqlService.update()
        mongoDBService.update()
    }

    @GetMapping("/read")
    fun read() {
        mysqlService.read()
        postgresqlService.read()
        mongoDBService.read()
    }

    @GetMapping("/delete")
    fun delete() {
        mysqlService.delete()
        postgresqlService.delete()
        mongoDBService.delete()
    }
}
