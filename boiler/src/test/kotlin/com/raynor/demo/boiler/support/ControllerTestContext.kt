package com.raynor.demo.boiler.support

import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(
    value = [],
)
@Import()
open class ControllerTestContext(
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
