package com.raynor.demo.batchbulkexcel.support

import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.utility.DockerImageName
import java.nio.file.Files
import java.nio.file.Path

/**
 * 모든 통합 테스트(@SpringBootTest)의 공통 베이스.
 * MySQL 컨테이너와 로컬 저장소 디렉터리를 **모듈 전체에서 1개만** 띄워 공유한다.
 * 프로퍼티 값이 스펙마다 동일하므로 Spring 컨텍스트도 캐시 재사용된다.
 * `@SpringBootTest`/`@AutoConfigureMockMvc`/`@DynamicPropertySource` 는 클래스 계층에서 상속 탐색된다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ApplyExtension(SpringExtension::class)
abstract class IntegrationTestSpec(
    body: FunSpec.() -> Unit = {},
) : FunSpec(body) {
    companion object {
        @Container
        @JvmStatic
        val mysql =
            MySQLContainer(DockerImageName.parse("mysql:8.0")).apply {
                start()
            }

        @JvmStatic
        val storageDir: Path = Files.createTempDirectory("excel-it")

        init {
            Runtime.getRuntime().addShutdownHook(Thread { storageDir.toFile().deleteRecursively() })
        }

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("storage.datasource.core.jdbc-url") { mysql.jdbcUrl }
            registry.add("storage.datasource.core.username") { mysql.username }
            registry.add("storage.datasource.core.password") { mysql.password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "validate" }
            registry.add("app.storage.local.base-dir") { storageDir.toString() }
        }
    }
}
