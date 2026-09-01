package com.raynor.demo.boiler.support

import com.ninjasquad.springmockk.MockkBean
import com.raynor.demo.boiler.controller.product.ProductController
import com.raynor.demo.boiler.service.product.ProductService
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(
    value = [
        ProductController::class,
    ],
)
@Import()
@ApplyExtension(SpringExtension::class)
abstract class ControllerTestContext(
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
    ) {
    @MockkBean
    private lateinit var productService: ProductService
}
