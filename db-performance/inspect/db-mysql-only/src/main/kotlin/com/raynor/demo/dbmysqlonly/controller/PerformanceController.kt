package com.raynor.demo.dbmysqlonly.controller

import com.raynor.demo.dbmysqlonly.service.MySQL5Service
import com.raynor.demo.dbmysqlonly.service.MySQL8Service
import com.raynor.demo.dbmysqlonly.service.MySQL9Service
import com.raynor.demo.dbmysqlonly.service.PerformanceService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.*

@RestController
class PerformanceController(
    @Qualifier(MySQL5Service.BEAN_NAME) private val mysql5Service: PerformanceService,
    @Qualifier(MySQL8Service.BEAN_NAME) private val mysql8Service: PerformanceService,
    @Qualifier(MySQL9Service.BEAN_NAME) private val mysql9Service: PerformanceService,
) {

    /* MySQL 5 */
    @GetMapping("/mysql5/persons/{id}")
    fun mysql5SimpleSelect(@PathVariable id: Int) {
        mysql5Service.simpleSelect(id)
    }

    @GetMapping("/mysql5/persons")
    fun mysql5ListSelect() {
        mysql5Service.listSelect()
    }

    @PostMapping("/mysql5/persons")
    fun mysql5IndividualInsert() {
        mysql5Service.individualInsert()
    }

    @PostMapping("/mysql5/persons/bulk-insert")
    fun mysql5BulkInsert() {
        mysql5Service.bulkInsert()
    }

    @PatchMapping("/mysql5/persons/bulk-update")
    fun mysql5BulkUpdate() {
        mysql5Service.bulkUpdate()
    }

    @DeleteMapping("/mysql5/persons/{id}")
    fun mysql5IndividualDelete(@PathVariable id: Int) {
        mysql5Service.individualDelete(id)
    }

    /* MySQL 8 */
    @GetMapping("/mysql8/persons/{id}")
    fun mysql8SimpleSelect(@PathVariable id: Int) {
        mysql8Service.simpleSelect(id)
    }

    @GetMapping("/mysql8/persons")
    fun mysql8ListSelect() {
        mysql8Service.listSelect()
    }

    @PostMapping("/mysql8/persons")
    fun mysql8IndividualInsert() {
        mysql8Service.individualInsert()
    }

    @PostMapping("/mysql8/persons/bulk-insert")
    fun mysql8BulkInsert() {
        mysql8Service.bulkInsert()
    }

    @PatchMapping("/mysql8/persons/bulk-update")
    fun mysql8BulkUpdate() {
        mysql8Service.bulkUpdate()
    }

    @DeleteMapping("/mysql8/persons/{id}")
    fun mysql8IndividualDelete(@PathVariable id: Int) {
        mysql8Service.individualDelete(id)
    }

    /* MySQL 9 */
    @GetMapping("/mysql9/persons/{id}")
    fun mysql9SimpleSelect(@PathVariable id: Int) {
        mysql9Service.simpleSelect(id)
    }

    @GetMapping("/mysql9/persons")
    fun mysql9ListSelect() {
        mysql9Service.listSelect()
    }

    @PostMapping("/mysql9/persons")
    fun mysql9IndividualInsert() {
        mysql9Service.individualInsert()
    }

    @PostMapping("/mysql9/persons/bulk-insert")
    fun mysql9BulkInsert() {
        mysql9Service.bulkInsert()
    }

    @PatchMapping("/mysql9/persons/bulk-update")
    fun mysql9BulkUpdate() {
        mysql9Service.bulkUpdate()
    }

    @DeleteMapping("/mysql9/persons/{id}")
    fun mysql9IndividualDelete(@PathVariable id: Int) {
        mysql9Service.individualDelete(id)
    }
}