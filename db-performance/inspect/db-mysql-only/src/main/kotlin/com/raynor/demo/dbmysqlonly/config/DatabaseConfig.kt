package com.raynor.demo.dbmysqlonly.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
class DatabaseConfig {

    companion object {
        const val MYSQL_5_DATA_SOURCE = "mysql5DataSource"
        const val MYSQL_8_DATA_SOURCE = "mysql8DataSource"
        const val MYSQL_9_DATA_SOURCE = "mysql9DataSource"

        const val MYSQL_5_JDBC_TEMPLATE = "mysql5JdbcTemplate"
        const val MYSQL_8_JDBC_TEMPLATE = "mysql8JdbcTemplate"
        const val MYSQL_9_JDBC_TEMPLATE = "mysql9JdbcTemplate"
    }


    @Bean(MYSQL_5_DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.mysql5")
    fun mysql5DataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean(MYSQL_8_DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.mysql8")
    fun mysql8DataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean(MYSQL_9_DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.mysql9")
    fun mysql9DataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean(MYSQL_5_JDBC_TEMPLATE)
    fun mysql5JdbcTemplate(
        @Qualifier(MYSQL_5_DATA_SOURCE) mysql5DataSource: DataSource
    ): JdbcTemplate {
        return JdbcTemplate(mysql5DataSource)
    }

    @Bean(MYSQL_8_JDBC_TEMPLATE)
    fun mysql8JdbcTemplate(
        @Qualifier(MYSQL_8_DATA_SOURCE) mysql8DataSource: DataSource
    ): JdbcTemplate {
        return JdbcTemplate(mysql8DataSource)
    }

    @Bean(MYSQL_9_JDBC_TEMPLATE)
    fun mysql9JdbcTemplate(
        @Qualifier(MYSQL_9_DATA_SOURCE) mysql9DataSource: DataSource
    ): JdbcTemplate {
        return JdbcTemplate(mysql9DataSource)
    }
}