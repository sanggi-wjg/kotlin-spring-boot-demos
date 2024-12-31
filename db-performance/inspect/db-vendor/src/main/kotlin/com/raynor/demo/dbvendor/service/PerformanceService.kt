package com.raynor.demo.dbvendor.service

interface PerformanceService {
    fun create()
    fun update()
    fun read()
    fun readByPk()
    fun readByNoneIndexColumn()
    fun delete()
}
