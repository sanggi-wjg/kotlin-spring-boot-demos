package com.raynor.demo.dbmysqlonly.controller

import com.raynor.demo.dbmysqlonly.service.MySQL5Service
import com.raynor.demo.dbmysqlonly.service.MySQL8Service
import com.raynor.demo.dbmysqlonly.service.MySQL9Service
import com.raynor.demo.dbmysqlonly.service.PerformanceService
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
        mysql5Service.simpleSelect()
        mysql8Service.simpleSelect()
        mysql9Service.simpleSelect()
    }

    @GetMapping("/list-select")
    fun listSelect() {
        mysql5Service.listSelect()
        mysql8Service.listSelect()
        mysql9Service.listSelect()
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

    @GetMapping("/ind-update")
    fun individualUpdate() {
        mysql5Service.individualUpdate()
        mysql8Service.individualUpdate()
        mysql9Service.individualUpdate()
    }

    @GetMapping("/bulk-update")
    fun bulkUpdate() {
        mysql5Service.bulkUpdate()
        mysql8Service.bulkUpdate()
        mysql9Service.bulkUpdate()
    }

    @GetMapping("/ind-delete")
    fun individualDelete() {
        mysql5Service.individualDelete()
        mysql8Service.individualDelete()
        mysql9Service.individualDelete()
    }
}