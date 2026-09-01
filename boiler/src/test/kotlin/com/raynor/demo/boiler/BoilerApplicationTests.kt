package com.raynor.demo.boiler

import com.raynor.demo.boiler.support.TestContainersConfig
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(
    value = [
        TestContainersConfig::class,
    ],
)
@ApplyExtension(SpringExtension::class)
class BoilerApplicationTests(
    private val application: BoilerApplication,
) : FunSpec({
        test("context loads") { }
    })
