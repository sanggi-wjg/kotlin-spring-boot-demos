package com.raynor.demo.aboutfeign.app.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(basePackages = ["com.raynor.demo.aboutfeign"])
class OpenFeignConfig
