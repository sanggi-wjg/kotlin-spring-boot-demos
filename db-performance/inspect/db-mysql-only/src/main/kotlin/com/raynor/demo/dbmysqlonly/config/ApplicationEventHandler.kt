package com.raynor.demo.dbmysqlonly.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class ApplicationEventHandler(
    @Qualifier(DataSourceConfig.MYSQL_5_JDBC_TEMPLATE) private val mysql5JdbcTemplate: JdbcTemplate,
    @Qualifier(DataSourceConfig.MYSQL_8_JDBC_TEMPLATE) private val mysql8JdbcTemplate: JdbcTemplate,
    @Qualifier(DataSourceConfig.MYSQL_9_JDBC_TEMPLATE) private val mysql9JdbcTemplate: JdbcTemplate,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @EventListener(ApplicationStartedEvent::class)
    fun onApplicationStartedEvent(event: ApplicationStartedEvent) {
        logger.info("== Cleaning databases start!")
        cleanMySQLDatabase()
        logger.info("== Cleaning databases finished!")
    }

    private fun cleanMySQLDatabase() {
        "DROP TABLE IF EXISTS `person`".also {
            mysql5JdbcTemplate.execute(it)
            mysql8JdbcTemplate.execute(it)
            mysql9JdbcTemplate.execute(it)
        }
        """
        CREATE TABLE `person` (
            `id` INT NOT NULL AUTO_INCREMENT,
            `name` VARCHAR(255) NOT NULL,
            `age` INT NOT NULL,
            `is_active` BOOLEAN NOT NULL,
            `created_at` DATETIME NOT NULL,
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        """.trimIndent().also {
            mysql5JdbcTemplate.execute(it)
        }

        """
        CREATE TABLE `person` (
            `id` INT NOT NULL AUTO_INCREMENT,
            `name` VARCHAR(255) NOT NULL,
            `age` INT NOT NULL,
            `is_active` BOOLEAN NOT NULL,
            `created_at` DATETIME NOT NULL,
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
        """.trimIndent().also {
            mysql8JdbcTemplate.execute(it)
            mysql9JdbcTemplate.execute(it)
        }
    }
}