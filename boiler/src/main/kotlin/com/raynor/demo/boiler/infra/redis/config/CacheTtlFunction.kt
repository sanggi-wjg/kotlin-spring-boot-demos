package com.raynor.demo.boiler.infra.redis.config

import org.springframework.data.redis.cache.RedisCacheWriter
import java.time.Duration

class CacheTtlFunction : RedisCacheWriter.TtlFunction {

    /*
     * 사용 필요한 상황
     * 1. 값의 내용에 따라 TTL을 정할 때
     *   - 이벤트 종료시간까지 캐시하고 싶다든지
     * 2. 만료 시각을 분산시킬 때 (jitter)
     *   - 동시 만료시 DB 조회가 몰리는 것 방지, cache stampede
     * 3. 결과의 종류에 따라 다르게 할 때
     *   - empty 등은 짧게 성공은 길게 등
     *  */
    override fun getTimeToLive(
        key: Any,
        value: Any?,
    ): Duration {
        return Duration.ofMinutes(1)
    }
}
