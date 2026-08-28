package com.raynor.demo.batchbulkexcel.config

import com.raynor.demo.batchbulkexcel.storage.rds.enum.OrderStatus
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class ApplicationEventListener(
    private val jdbcTemplate: JdbcTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TARGET = 100_000L
    }

    @EventListener(ApplicationReadyEvent::class)
    fun seed() {
        seedUsers()
        seedUserMileages()
        seedOrders()
        seedOrderItems()
    }

    private fun seedUsers() {
        val current = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long::class.java) ?: 0
        if (current >= TARGET) {
            log.info("user: {} (>= {}) → skip", current, TARGET)
            return
        }

        val rows = ((current + 1)..TARGET).map { arrayOf<Any>("user-$it") }
        jdbcTemplate.batchUpdate("INSERT INTO users (name) VALUES (?)", rows)
        log.info("user: {} → {} (+{})", current, TARGET, rows.size)
    }

    private fun seedUserMileages() {
        val inserted =
            jdbcTemplate.update(
                """
                INSERT INTO user_mileage (user_id, balance, updated_at)
                SELECT u.id, 0, NOW()
                FROM users u
                WHERE NOT EXISTS (SELECT 1 FROM user_mileage m WHERE m.user_id = u.id)
                """.trimIndent(),
            )
        log.info("user_mileage: +{}", inserted)
    }

    private fun seedOrders() {
        val current = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long::class.java) ?: 0
        if (current >= TARGET) {
            log.info("orders: {} (>= {}) → skip", current, TARGET)
            return
        }

        val statuses = OrderStatus.entries
        val rows =
            (1..(TARGET - current)).map {
                arrayOf<Any>(
                    Random.nextLong(1, TARGET + 1),
                    Random.nextLong(1_000, 1_000_000),
                    statuses.random().name,
                )
            }
        jdbcTemplate.batchUpdate("INSERT INTO orders (user_id, total_price, status) VALUES (?, ?, ?)", rows)
        log.info("orders: {} → {} (+{})", current, TARGET, rows.size)
    }

    private fun seedOrderItems() {
        val inserted =
            jdbcTemplate.update(
                """
                INSERT INTO order_item (order_id, product_name, quantity, price)
                SELECT o.id,
                       ELT(FLOOR(1 + RAND() * 6), 'Keyboard', 'Mouse', 'Monitor', 'Cable', 'Webcam', 'Headset'),
                       FLOOR(1 + RAND() * 5),
                       FLOOR(1000 + RAND() * 499000)
                FROM orders o
                WHERE NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.id)
                """.trimIndent(),
            )
        log.info("order_item: +{}", inserted)
    }
}
