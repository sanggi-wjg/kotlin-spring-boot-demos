package com.raynor.demo.productservice

import com.raynor.demo.productservice.rds.config.JpaConfig
import com.raynor.demo.productservice.rds.config.QueryDslConfig
import com.raynor.demo.productservice.service.ProductService
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(
    classes = [
        ProductService::class
    ]
)
@Import(
    value = [
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        TransactionAutoConfiguration::class,
//        DatabaseConfig::class,
        JpaConfig::class,
        QueryDslConfig::class,
        JacksonAutoConfiguration::class,
    ]
)
@TestConfiguration
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ServiceTestContext(
    body: FunSpec.() -> Unit = {}
) : FunSpec({

    afterEach {
        clearAllMocks()
    }

    afterTest {
        unmockkAll()
    }

    body()
}) {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        private val mysqlContainer = MySQLContainer("mysql:8.0")
            .withDatabaseName("msa_product")
            .withUsername("test_user")
            .withPassword("passw0rd")
            .withReuse(true)
    }
}