package com.raynor.demo.boiler.service

import com.raynor.demo.boiler.service.product.ProductService
import com.raynor.demo.boiler.support.ServiceTestContext

class ProductServiceTest(
    private val productService: ProductService,
) : ServiceTestContext(
        {
            test("123") {
                val result = productService.getProducts(10, null)
                print(result)
            }
        },
    )
