package com.raynor.demo.dbvendor.controller

import com.raynor.demo.dbvendor.service.MySQLPerformanceService
import com.raynor.demo.dbvendor.service.PerformanceService
import com.raynor.demo.dbvendor.service.PostgreSQLPerformanceService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PerformanceController(
    @Qualifier(MySQLPerformanceService.BEAN_NAME) private val mysqlService: PerformanceService,
    @Qualifier(PostgreSQLPerformanceService.BEAN_NAME) private val postgresqlService: PerformanceService,
) {

    @GetMapping("/simple-insert")
    fun simpleInsert() {
        mysqlService.insert()
        postgresqlService.insert()
    }
}