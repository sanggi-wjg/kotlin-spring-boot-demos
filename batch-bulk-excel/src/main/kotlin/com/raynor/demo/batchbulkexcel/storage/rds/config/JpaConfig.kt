package com.raynor.demo.batchbulkexcel.storage.rds.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
@EntityScan(
    basePackages = ["com.raynor.demo.batchbulkexcel.storage.rds.entity"],
)
@EnableJpaRepositories(
    basePackages = ["com.raynor.demo.batchbulkexcel.storage.rds.repository"],
)
class JpaConfig
