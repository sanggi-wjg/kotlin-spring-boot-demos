package com.raynor.demo.dbperformance.service

interface PerformanceService {
    fun individualInsert()

    fun bulkInsert()

    fun simpleSelect()

    fun relatedSelect()

    fun individualUpdate()

    fun bulkUpdate()

    fun delete()
}