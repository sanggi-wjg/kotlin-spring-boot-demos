package com.raynor.demo.dbperformance.controller

import com.raynor.demo.dbperformance.service.MySQL5Service
import com.raynor.demo.dbperformance.service.MySQL8Service
import com.raynor.demo.dbperformance.service.MySQL9Service
import com.raynor.demo.dbperformance.service.PerformanceService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PerformanceController(
    @Qualifier(MySQL5Service.BEAN_NAME) private val mysql5Service: PerformanceService,
    @Qualifier(MySQL8Service.BEAN_NAME) private val mysql8Service: PerformanceService,
    @Qualifier(MySQL9Service.BEAN_NAME) private val mysql9Service: PerformanceService,
) {

    @GetMapping("/simple-select")
    fun simpleSelect() {
        mysql8Service.simpleSelect()
    }

    @GetMapping("/ind-insert")
    fun individualInsert() {
        mysql5Service.individualInsert()
        mysql8Service.individualInsert()
        mysql9Service.individualInsert()
    }

    @GetMapping("/bulk-insert")
    fun bulkInsert() {
        mysql5Service.bulkInsert()
        mysql8Service.bulkInsert()
        mysql9Service.bulkInsert()
    }

    @GetMapping("/bulk-update")
    fun bulkUpdate() {
        mysql5Service.bulkUpdate()
        mysql8Service.bulkUpdate()
        mysql9Service.bulkUpdate()
    }

    @GetMapping("delete")
    fun delete() {
        mysql5Service.delete()
        mysql8Service.delete()
        mysql9Service.delete()
    }
}