package com.raynor.demo.redis.app.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@Configuration
@EnableCaching
class RedisCacheConfig {

    @Bean
    fun cacheManager(
        connectionFactory: RedisConnectionFactory,
    ): RedisCacheManager {
        // https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html
        val defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(60))
            .disableCachingNullValues()

        return RedisCacheManager.builder(connectionFactory)
            .transactionAware()
            .cacheDefaults(defaultCacheConfig)
            .withInitialCacheConfigurations(
                mapOf(
                    CacheConfigKey.DAYS_TTL to defaultCacheConfig.entryTtl(Duration.ofDays(1))
                )
            )
            .build()
    }
}