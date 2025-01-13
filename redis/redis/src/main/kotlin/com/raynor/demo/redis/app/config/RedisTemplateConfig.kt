package com.raynor.demo.redis.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.ValueOperations
import org.springframework.data.redis.serializer.GenericToStringSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisTemplateConfig {

    companion object {
        const val BEAN_NAME_VALUE_OPS_WITH_BYTES = "valueOpsWithBytes"
    }

    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            this.connectionFactory = connectionFactory
            this.keySerializer = StringRedisSerializer()
            this.valueSerializer = StringRedisSerializer()
            this.setEnableTransactionSupport(true)
        }
    }

    @Bean
    fun redisTemplateWithBytes(
        connectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, ByteArray> {
        return RedisTemplate<String, ByteArray>().apply {
            this.connectionFactory = connectionFactory
            this.keySerializer = StringRedisSerializer()
            this.valueSerializer = GenericToStringSerializer(ByteArray::class.java)
            this.setEnableTransactionSupport(true)
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
    fun valueOperations(
        redisTemplate: RedisTemplate<String, String>,
    ): ValueOperations<String, String> {
        return redisTemplate.opsForValue()
    }

    @Bean(BEAN_NAME_VALUE_OPS_WITH_BYTES)
    fun valueOperationsWithBytes(
        redisTemplateWithBytes: RedisTemplate<String, ByteArray>,
    ): ValueOperations<String, ByteArray> {
        return redisTemplateWithBytes.opsForValue()
    }
}