package com.raynor.demo.dbmysqlonly.service

import com.raynor.demo.dbmysqlonly.service.model.Person
import com.raynor.demo.support.annotation.Benchmark
import com.raynor.demo.support.config.Constants.BATCH_SIZE
import com.raynor.demo.support.config.Constants.CYCLE_COUNT
import com.raynor.demo.support.config.Constants.INSERT_SIZE
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.Instant

abstract class MySQLService(
    private val jdbcTemplate: JdbcTemplate
) : PerformanceService {

    @Benchmark
    override fun simpleSelect() {
        repeat(CYCLE_COUNT) {
            val sql = "SELECT * FROM person WHERE id = ?"
            runCatching {
                jdbcTemplate.queryForObject(sql, Person::class.java, it + 1)
            }
        }
    }

    @Benchmark
    override fun listSelect() {
        repeat(CYCLE_COUNT) {
            val limit = it * 10
            val offset = it + 10
            val sql = "SELECT * FROM person LIMIT ? OFFSET ?"
            jdbcTemplate.queryForList(sql, limit, offset)
        }
    }

    @Benchmark
    override fun complexSelect() {
        TODO("Not yet implemented")
    }

    @Benchmark
    override fun individualInsert() {
        val now = Instant.now()
        val sql = "INSERT INTO person (name, age, is_active, created_at) VALUES (?, ?, ?, ?)"

        repeat(INSERT_SIZE) {
            jdbcTemplate.update(sql, "name", it, true, now)
        }
    }

    @Benchmark
    override fun bulkInsert() {
        val now = Instant.now()
        val timestamp = Timestamp.from(now)

        val sql = "INSERT INTO person (name, age, is_active, created_at) VALUES (?, ?, ?, ?)"
        val persons = List(INSERT_SIZE) {
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
            sql, persons, BATCH_SIZE
        ) { ps, person ->
            ps.setString(1, person.name)
            ps.setInt(2, person.age)
            ps.setBoolean(3, person.isActive)
            ps.setTimestamp(4, person.createdAtTimeStamp)
        }
    }

    @Benchmark
    override fun individualUpdate() {
        val now = Instant.now()

        repeat(CYCLE_COUNT) {
            val sql = "UPDATE person SET name = ?, age = ?, is_active = ?, created_at = ? WHERE id = ?"
            jdbcTemplate.update(sql, "name changed", it, false, now, it)
        }
    }

    @Benchmark
    override fun bulkUpdate() {
        val now = Instant.now()
        val timestamp = Timestamp.from(now)

        val sql = "UPDATE person SET name = ?, age = ?, is_active = ?, created_at = ? WHERE id = ?"
        val persons = List(CYCLE_COUNT) {
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
            sql, persons, BATCH_SIZE
        ) { ps, person ->
            ps.setString(1, person.name)
            ps.setInt(2, person.age)
            ps.setBoolean(3, person.isActive)
            ps.setTimestamp(4, person.createdAtTimeStamp)
            ps.setInt(5, person.id!!)
        }
    }

    @Benchmark
    override fun individualDelete() {
        repeat(CYCLE_COUNT) { id ->
            val sql = "DELETE FROM person WHERE id = ?"
            jdbcTemplate.update(sql, id + 1)
        }
    }
}