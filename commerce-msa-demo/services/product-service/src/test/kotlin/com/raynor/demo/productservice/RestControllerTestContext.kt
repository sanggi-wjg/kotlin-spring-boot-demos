package com.raynor.demo.productservice

import com.ninjasquad.springmockk.MockkBean
import com.raynor.demo.productservice.api.ProductRestController
import com.raynor.demo.productservice.service.ProductService
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(
    value = [
        ProductRestController::class,
    ],
)
@Import(
    value = [
        JacksonAutoConfiguration::class,
    ]
)
@EnableConfigurationProperties(value = [])
open class RestControllerTestContext(
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
    @MockkBean
    private lateinit var productService: ProductService
}
