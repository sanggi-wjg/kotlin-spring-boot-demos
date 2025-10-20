package com.raynor.demo.orderservice.rds.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EntityScan(
    basePackages = [JpaConfig.ENTITY_PACKAGE_ROOT]
)
@EnableJpaRepositories(
    basePackages = [JpaConfig.REPOSITORY_PACKAGE_ROOT]
)
class JpaConfig {

    companion object {
        private const val BASE_PACKAGE_ROOT = "com.raynor.demo.orderservice.rds"
        const val ENTITY_PACKAGE_ROOT = "$BASE_PACKAGE_ROOT.entity"
        const val REPOSITORY_PACKAGE_ROOT = "$BASE_PACKAGE_ROOT.repository"
    }
}