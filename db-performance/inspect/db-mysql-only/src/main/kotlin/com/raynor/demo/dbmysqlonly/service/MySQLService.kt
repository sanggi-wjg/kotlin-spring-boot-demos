package com.raynor.demo.dbmysqlonly.service

import com.raynor.demo.dbmysqlonly.config.Constants
import com.raynor.demo.dbmysqlonly.service.model.Person
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.Instant

abstract class MySQLService(
    private val jdbcTemplate: JdbcTemplate
) : PerformanceService {

    override fun simpleSelect(id: Int): Person? {
        val sql = "SELECT * FROM person WHERE id = $id"
        return jdbcTemplate.queryForObject(sql, Person::class.java)
    }

    override fun listSelect(): List<Person> {
        val sql = "SELECT * FROM person"
        return jdbcTemplate.queryForList(sql, Person::class.java)
    }

    override fun complexSelect() {
        TODO("Not yet implemented")
    }

    override fun individualInsert() {
        val now = Instant.now()
        val sql = "INSERT INTO person (name, age, is_active, created_at) VALUES (?, ?, ?, ?)"

        repeat(Constants.DB_REPEAT_COUNT) {
            jdbcTemplate.update(sql, "name", it, true, now)
        }
    }

    override fun bulkInsert() {
        val now = Instant.now()
        val timestamp = Timestamp.from(now)

        val sql = "INSERT INTO person (name, age, is_active, created_at) VALUES (?, ?, ?, ?)"
        val persons = List(Constants.DB_REPEAT_COUNT) {
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
            sql, persons, Constants.DB_BATCH_SIZE
        ) { ps, person ->
            ps.setString(1, person.name)
            ps.setInt(2, person.age)
            ps.setBoolean(3, person.isActive)
            ps.setTimestamp(4, person.createdAtTimeStamp)
        }
    }

    override fun individualUpdate() {
        TODO("Not yet implemented")
    }

    override fun bulkUpdate() {
        val now = Instant.now()
        val timestamp = Timestamp.from(now)

        val sql = "UPDATE person SET name = ?, age = ?, is_active = ?, created_at = ? WHERE id = ?"
        val persons = List(Constants.DB_REPEAT_COUNT) {
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
            sql, persons, Constants.DB_BATCH_SIZE
        ) { ps, person ->
            ps.setString(1, person.name)
            ps.setInt(2, person.age)
            ps.setBoolean(3, person.isActive)
            ps.setTimestamp(4, person.createdAtTimeStamp)
            ps.setInt(5, person.id!!)
        }
    }

    override fun individualDelete(id: Int) {
        jdbcTemplate.execute("DELETE FROM person WHERE id = $id")
    }
}