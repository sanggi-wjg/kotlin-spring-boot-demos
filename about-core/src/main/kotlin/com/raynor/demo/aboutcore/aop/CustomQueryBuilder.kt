package com.raynor.demo.aboutcore.aop

import org.springframework.stereotype.Component

@Component
class CustomQueryBuilder {
    private var query: String = ""

    fun select(q: String): CustomQueryBuilder {
        this.query += "SELECT $q\n"
        return this
    }

    fun from(q: String): CustomQueryBuilder {
        this.query += "FROM $q\n"
        return this
    }

    fun where(q: String): CustomQueryBuilder {
        this.query += "WHERE $q\n"
        return this
    }

    fun build(): String {
        val q = this.query
        this.query = ""
        return q
    }
}

fun String.setComment(): String {
    return "/* query comment is here */\nand query is below maybe.\n$this"
}