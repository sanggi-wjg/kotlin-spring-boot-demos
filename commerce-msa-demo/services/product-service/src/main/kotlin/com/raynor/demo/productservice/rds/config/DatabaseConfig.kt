package com.raynor.demo.productservice.rds.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    fun dataSourceProperties(): HikariConfig {
        return HikariConfig()
    }

    @Bean
    fun dataSource(properties: HikariConfig): HikariDataSource {
        return HikariDataSource(properties)
    }
}