package com.raynor.demo.productservice

import io.kotest.core.spec.style.FunSpec
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ProductServiceApplicationTest(
    private val application: ProductServiceApplication
) : FunSpec({
    test("어플리케이션 테스트") {}
})
