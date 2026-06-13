package com.raynor.demo.batchbulkexcel.storage.rds.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "storage.datasource")
@Configuration
class DatabaseConfig {
    companion object {
        const val DATASOURCE_PREFIX = "storage.datasource.core"
        const val HIKARI_CONFIG_BEAN = "hikariConfig"
    }

    @Bean(HIKARI_CONFIG_BEAN)
    @ConfigurationProperties(prefix = DATASOURCE_PREFIX)
    fun hikariConfig(): HikariConfig = HikariConfig()

    @Bean
    fun dataSourceProperties(
        @Qualifier(HIKARI_CONFIG_BEAN) hikariConfig: HikariConfig,
    ): HikariDataSource = HikariDataSource(hikariConfig)
}
