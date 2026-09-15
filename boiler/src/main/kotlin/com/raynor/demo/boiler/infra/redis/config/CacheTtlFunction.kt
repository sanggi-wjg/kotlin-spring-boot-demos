package com.raynor.demo.boiler.infra.redis.config

import org.springframework.data.redis.cache.RedisCacheWriter
import java.time.Duration

class CacheTtlFunction : RedisCacheWriter.TtlFunction {
    override fun getTimeToLive(
        key: Any,
        value: Any?,
    ): Duration {
        return Duration.ofMinutes(1)
    }
}
