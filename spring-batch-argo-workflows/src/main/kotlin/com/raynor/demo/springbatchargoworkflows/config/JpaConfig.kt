package com.raynor.demo.springbatchargoworkflows.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
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
        private const val BASE_PACKAGE_ROOT = "com.raynor.demo.springbatchargoworkflows"
        const val ENTITY_PACKAGE_ROOT = "$BASE_PACKAGE_ROOT.entity"
        const val REPOSITORY_PACKAGE_ROOT = "$BASE_PACKAGE_ROOT.repository"
    }
}