package com.raynor.demo.redis.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun connectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory(
            RedisStandaloneConfiguration().apply {
                this.database = 9
                this.hostName = "localhost"
                this.port = 6379
            }
        )
    }

    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            this.connectionFactory = connectionFactory
            this.keySerializer = StringRedisSerializer()
            this.valueSerializer = StringRedisSerializer()
        }
    }


    @Bean
    fun listOperations(
        redisTemplate: RedisTemplate<String, String>
    ): ListOperations<String, String> {
        return redisTemplate.opsForList()
    }

    @Bean
    fun setOperations(
        redisTemplate: RedisTemplate<String, String>,
    ): SetOperations<String, String> {
        return redisTemplate.opsForSet()
    }

    @Bean
    fun cacheManager(
        connectionFactory: RedisConnectionFactory,
    ): RedisCacheManager {
        return RedisCacheManager.create(connectionFactory)
    }
}