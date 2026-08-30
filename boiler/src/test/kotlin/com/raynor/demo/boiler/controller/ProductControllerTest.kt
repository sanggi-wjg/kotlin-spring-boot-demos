package com.raynor.demo.boiler.controller

import com.raynor.demo.boiler.service.product.ProductService
import com.raynor.demo.boiler.support.ControllerTestContext
import io.kotest.matchers.shouldBe

class ProductControllerTest(
    private val productService: ProductService,
) : ControllerTestContext(
        {
            test("123") {
                1 shouldBe 1
            }
        },
    )
