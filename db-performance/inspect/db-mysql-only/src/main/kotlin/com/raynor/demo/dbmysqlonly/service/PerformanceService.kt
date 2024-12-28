package com.raynor.demo.dbmysqlonly.service

interface PerformanceService {
    fun simpleSelect()

    fun listSelect()

    fun complexSelect()

    fun individualInsert()

    fun bulkInsert()

    fun individualUpdate()

    fun bulkUpdate()

    fun individualDelete()
}