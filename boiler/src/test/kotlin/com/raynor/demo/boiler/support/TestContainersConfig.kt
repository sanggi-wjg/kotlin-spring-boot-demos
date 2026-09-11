package com.raynor.demo.boiler.support

import com.redis.testcontainers.RedisContainer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.mysql.MySQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestContainersConfig {
    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer =
        MySQLContainer("mysql:8.0")
            .withCommand("--default-time-zone=+00:00")
            .withReuse(true)

    @Bean
    fun redisContainer(): RedisContainer =
        RedisContainer("redis:latest")
            .withReuse(true)

    @Bean
    fun mysqlPropertyRegistrar(container: MySQLContainer): DynamicPropertyRegistrar =
        DynamicPropertyRegistrar { registry ->
            registry.add("spring.datasource.hikari.jdbc-url", container::getJdbcUrl)
            registry.add("spring.datasource.hikari.username", container::getUsername)
            registry.add("spring.datasource.hikari.password", container::getPassword)
        }

    @Bean
    fun redisPropertyRegistrar(container: RedisContainer): DynamicPropertyRegistrar =
        DynamicPropertyRegistrar { registry ->
            registry.add("spring.data.redis.host", container::getHost)
            registry.add("spring.data.redis.port", container::getFirstMappedPort)
        }
}
