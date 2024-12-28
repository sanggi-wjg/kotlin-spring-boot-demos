package com.raynor.demo.dbmysqlonly.service

import com.raynor.demo.dbmysqlonly.service.model.Person

interface PerformanceService {
    fun simpleSelect(id: Int): Person?

    fun listSelect(): List<Person>

    fun complexSelect()

    fun individualInsert()

    fun bulkInsert()

    fun individualUpdate()

    fun bulkUpdate()

    fun individualDelete(id: Int)
}