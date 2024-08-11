package com.raynor.demo.aboutfeign.app.config

import feign.Retryer
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(basePackages = ["com.raynor.demo.aboutfeign"])
class OpenFeignConfig {

    @Bean
    fun retryer() =
        // 1초 -> 최대 5초 간격으로 3번 반복
        Retryer.Default(1000, 5000, 3)
}
