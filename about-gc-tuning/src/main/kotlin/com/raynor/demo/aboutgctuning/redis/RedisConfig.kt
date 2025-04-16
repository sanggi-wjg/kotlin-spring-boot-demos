package com.raynor.demo.aboutgctuning.redis

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@EnableCaching
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
}