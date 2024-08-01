package com.raynor.demo.redis.app.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@Configuration
@EnableRedisRepositories(
    basePackages = ["com.raynor.demo.redis.app.repository"],
)
class RedisRepositoryConfig
