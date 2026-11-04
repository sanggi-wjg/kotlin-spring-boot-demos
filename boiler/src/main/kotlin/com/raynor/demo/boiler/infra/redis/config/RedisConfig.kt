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
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.KotlinModule
import java.time.Duration

// @EnableRedisRepositories(basePackages = ["com.raynor.demo.boiler.infra.redis"])
@EnableCaching
@Configuration
class RedisConfig {

    private val cacheValueSerializer by lazy {
        GenericJacksonJsonRedisSerializer.builder()
            .customize { it.addModule(KotlinModule.Builder().build()) }
            .enableDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType("com.raynor.demo.boiler.")
                    .allowIfSubType("java.")
                    .build(),
            )
            .build()
    }

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
        val config = defaultCacheConfig()
            .entryTtl(CacheTtlFunction())

        val cacheConfig = mutableMapOf<String, RedisCacheConfiguration>()
        cacheConfig[CacheKey.COUPON_SCHEME] = defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60))

        return RedisCacheManager.builder(redisConnectionFactory)
            .transactionAware()
            .withInitialCacheConfigurations(cacheConfig)
            .cacheDefaults(config)
            .build()
    }

    private fun defaultCacheConfig(): RedisCacheConfiguration {
        return RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()),
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(cacheValueSerializer),
            )
    }
}
