package com.raynor.demo.dbperformance.service

import com.raynor.demo.dbperformance.config.Constants
import com.raynor.demo.dbperformance.model.Person
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.Instant

abstract class MySQLService(
    private val jdbcTemplate: JdbcTemplate
) : PerformanceService {

    override fun individualInsert() {
        val now = Instant.now()
        val sql = "INSERT INTO person (name, age, is_active, created_at) VALUES (?, ?, ?, ?)"

        repeat(Constants.DB_REPEAT_THRESHOLD) {
            jdbcTemplate.update(sql, "name", it, true, now)
        }
    }

    override fun bulkInsert() {
        val now = Instant.now()
        val timestamp = Timestamp.from(now)

        val sql = "INSERT INTO person (name, age, is_active, created_at) VALUES (?, ?, ?, ?)"
        val persons = List(Constants.DB_REPEAT_THRESHOLD) {
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
            sql, persons, Constants.BATCH_SIZE
        ) { ps, person ->
            ps.setString(1, person.name)
            ps.setInt(2, person.age)
            ps.setBoolean(3, person.isActive)
            ps.setTimestamp(4, person.createdAtTimeStamp)
        }
    }

    override fun simpleSelect() {
        val sql = "SELECT * FROM person"
        jdbcTemplate.execute(sql)
    }

    override fun relatedSelect() {
        TODO("Not yet implemented")
    }

    override fun individualUpdate() {
        TODO("Not yet implemented")
    }

    override fun bulkUpdate() {
        val now = Instant.now()
        val timestamp = Timestamp.from(now)

        val sql = "UPDATE person SET name = ?, age = ?, is_active = ?, created_at = ? WHERE id = ?"
        val persons = List(Constants.DB_REPEAT_THRESHOLD) {
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
            sql, persons, Constants.BATCH_SIZE
        ) { ps, person ->
            ps.setString(1, person.name)
            ps.setInt(2, person.age)
            ps.setBoolean(3, person.isActive)
            ps.setTimestamp(4, person.createdAtTimeStamp)
            ps.setInt(5, person.id!!)
        }
    }

    override fun delete() {
        jdbcTemplate.execute("DELETE FROM person")
    }
}