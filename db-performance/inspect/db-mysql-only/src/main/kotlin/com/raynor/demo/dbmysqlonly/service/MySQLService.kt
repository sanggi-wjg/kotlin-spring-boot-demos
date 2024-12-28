package com.raynor.demo.dbmysqlonly.service

import com.raynor.demo.dbmysqlonly.service.model.Person
import com.raynor.demo.support.config.Constants.DB_BATCH_SIZE
import com.raynor.demo.support.config.Constants.DB_REPEAT_COUNT
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.Instant

abstract class MySQLService(
    private val jdbcTemplate: JdbcTemplate
) : PerformanceService {

    override fun simpleSelect() {
        repeat(DB_REPEAT_COUNT) {
            val sql = "SELECT * FROM person WHERE id = ?"
            runCatching {
                jdbcTemplate.queryForObject(sql, Person::class.java, it + 1)
            }
        }
    }

    override fun listSelect() {
        repeat(DB_REPEAT_COUNT) {
            val limit = it * 10
            val offset = it + 10
            val sql = "SELECT * FROM person LIMIT ? OFFSET ?"
            jdbcTemplate.queryForList(sql, limit, offset)
        }
    }

    override fun complexSelect() {
        TODO("Not yet implemented")
    }

    override fun individualInsert() {
        val now = Instant.now()
        val sql = "INSERT INTO person (name, age, is_active, created_at) VALUES (?, ?, ?, ?)"

        repeat(DB_REPEAT_COUNT) {
            jdbcTemplate.update(sql, "name", it, true, now)
        }
    }

    override fun bulkInsert() {
        val now = Instant.now()
        val timestamp = Timestamp.from(now)

        val sql = "INSERT INTO person (name, age, is_active, created_at) VALUES (?, ?, ?, ?)"
        val persons = List(DB_REPEAT_COUNT) {
            Person(
                id = null,
                name = "name",
                age = it,
                isActive = true,
                createdAt = now,
                createdAtTimeStamp = timestamp
            )
        }

        jdbcTemplate.batchUpdate(
            sql, persons, DB_BATCH_SIZE
        ) { ps, person ->
            ps.setString(1, person.name)
            ps.setInt(2, person.age)
            ps.setBoolean(3, person.isActive)
            ps.setTimestamp(4, person.createdAtTimeStamp)
        }
    }

    override fun individualUpdate() {
        val now = Instant.now()

        repeat(DB_REPEAT_COUNT) {
            val sql = "UPDATE person SET name = ?, age = ?, is_active = ?, created_at = ? WHERE id = ?"
            jdbcTemplate.update(sql, "name changed", it, false, now, it)
        }
    }

    override fun bulkUpdate() {
        val now = Instant.now()
        val timestamp = Timestamp.from(now)

        val sql = "UPDATE person SET name = ?, age = ?, is_active = ?, created_at = ? WHERE id = ?"
        val persons = List(DB_REPEAT_COUNT) {
            Person(
                id = it,
                name = "name changed",
                age = it,
                isActive = false,
                createdAt = now,
                createdAtTimeStamp = timestamp
            )
        }

        jdbcTemplate.batchUpdate(
            sql, persons, DB_BATCH_SIZE
        ) { ps, person ->
            ps.setString(1, person.name)
            ps.setInt(2, person.age)
            ps.setBoolean(3, person.isActive)
            ps.setTimestamp(4, person.createdAtTimeStamp)
            ps.setInt(5, person.id!!)
        }
    }

    override fun individualDelete() {
        repeat(DB_REPEAT_COUNT) { id ->
            val sql = "DELETE FROM person WHERE id = ?"
            jdbcTemplate.update(sql, id + 1)
        }
    }
}