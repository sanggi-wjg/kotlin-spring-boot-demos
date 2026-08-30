package com.raynor.demo.boiler.support

import com.raynor.demo.boiler.config.DatabaseConfig
import com.raynor.demo.boiler.config.JpaConfig
import com.raynor.demo.boiler.config.QueryDslConfig
import com.raynor.demo.boiler.service.product.ProductService
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration
import org.springframework.context.annotation.Import

@SpringBootTest(
    classes = [
        ProductService::class,
    ],
)
@ImportAutoConfiguration(
    value = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        TransactionAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        DataJpaRepositoriesAutoConfiguration::class,
        FlywayAutoConfiguration::class,
    ],
)
@Import(
    value = [
        TestContainersConfig::class,
        DatabaseConfig::class,
        JpaConfig::class,
        QueryDslConfig::class,
    ],
)
@ApplyExtension(SpringExtension::class)
abstract class ServiceTestContext(
    body: FunSpec.() -> Unit = {},
) : FunSpec(
        {
            afterTest {
                unmockkAll()
            }

            afterEach {
                clearAllMocks()
            }

            body()
        },
    )
