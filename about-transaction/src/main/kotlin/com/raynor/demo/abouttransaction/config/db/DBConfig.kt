package com.raynor.demo.abouttransaction.config.db

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    basePackages = ["com.raynor.demo.abouttransaction.repository"],
)
class DBConfig
