package com.raynor.demo.boiler.infra.redis.config

import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@EnableCaching
@EnableRedisRepositories(basePackages = ["com.raynor.demo.boiler.repository"])
@Configuration
class RedisConfig {
    @Bean
    fun lettuceConnectionFactory(dataRedisProperties: DataRedisProperties): RedisConnectionFactory {
        return LettuceConnectionFactory(
            RedisStandaloneConfiguration(
                dataRedisProperties.host,
                dataRedisProperties.port,
            ),
        )
    }

    @Bean
    fun redisCacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(CacheTtlFunction())
            .disableCachingNullValues()

        return RedisCacheManager.builder(redisConnectionFactory)
            .transactionAware()
            .cacheDefaults(config)
            .build()
    }
}
