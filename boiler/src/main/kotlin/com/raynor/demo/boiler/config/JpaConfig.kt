package com.raynor.demo.boiler.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = ["com.raynor.demo.boiler.domain"])
@EnableJpaRepositories(basePackages = ["com.raynor.demo.boiler.domain"])
class JpaConfig
