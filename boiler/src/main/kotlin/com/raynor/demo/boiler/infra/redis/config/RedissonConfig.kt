package com.raynor.demo.boiler.infra.redis.config

import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RedissonConfig {
    @Bean
    fun redissonCustomizer(): RedissonAutoConfigurationCustomizer {
        return RedissonAutoConfigurationCustomizer { config ->
            config.setLockWatchdogTimeout(Duration.ofSeconds(30).toMillis())
        }
    }
}
