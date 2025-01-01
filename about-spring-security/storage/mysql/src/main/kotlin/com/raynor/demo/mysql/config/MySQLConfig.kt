package com.raynor.demo.mysql.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement


@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = ["com.raynor.demo.mysql.entity"])
@EnableJpaRepositories(basePackages = ["com.raynor.demo.mysql.repository"])
class MySQLConfig {

    companion object {
        const val DATA_SOURCE = "mysqlDataSource"
        const val DATA_SOURCE_PROPERTIES = "mysqlDataSourceProperties"
    }

    @Bean(DATA_SOURCE_PROPERTIES)
    @ConfigurationProperties(prefix = "storage.datasource.mysql")
    fun dataSourceProperties(): HikariConfig {
        return HikariConfig()
    }

    @Bean(DATA_SOURCE)
    fun dataSource(
        @Qualifier(DATA_SOURCE_PROPERTIES) dataSourceProperties: HikariConfig,
    ): HikariDataSource {
        return HikariDataSource(dataSourceProperties)
    }
}
