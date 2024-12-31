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
@RequestMapping("/independent")
class IndependentController(
    @Qualifier(MySQLPerformanceService.BEAN_NAME) private val mysqlService: PerformanceService,
    @Qualifier(PostgreSQLPerformanceService.BEAN_NAME) private val postgresqlService: PerformanceService,
    @Qualifier(MongoDBPerformanceService.BEAN_NAME) private val mongoDBService: PerformanceService,
) {

    @GetMapping("/mysql-create")
    fun mysqlCreate() {
        mysqlService.create()
    }

    @GetMapping("/postgresql-create")
    fun postgresqlCreate() {
        postgresqlService.create()
    }

    @GetMapping("/mysql-read-by-pk")
    fun mysqlRead() {
        mysqlService.readByPk()
    }
}