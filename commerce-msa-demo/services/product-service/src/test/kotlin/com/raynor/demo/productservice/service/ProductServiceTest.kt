package com.raynor.demo.productservice.service

import com.raynor.demo.productservice.ServiceTestContext
import com.raynor.demo.productservice.api.dto.response.ProductResponseDto
import com.raynor.demo.productservice.rds.entity.ProductEntity
import com.raynor.demo.productservice.rds.repository.ProductRdsRepository
import com.raynor.demo.productservice.service.condition.ProductSearchCondition
import com.raynor.demo.shared.typed.product.toProductId
import com.raynor.demo.shared.typed.product.toProductName
import com.raynor.demo.shared.typed.toMoney
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import java.math.BigDecimal

class ProductServiceTest(
    private val productRdsRepository: ProductRdsRepository,
    private val productService: ProductService,
) : ServiceTestContext({

    beforeEach {
        productRdsRepository.deleteAllInBatch()
    }

    context("상품 조회") {

        test("상품 리스트 조회") {
            // given
            val product = ProductEntity(
                name = "테스트",
                price = BigDecimal("10000.00"),
                stockQuantity = 10,
            )
            productRdsRepository.save(product)

            val expected = ProductResponseDto(
                id = 1L.toProductId(),
                name = product.name.toProductName(),
                price = product.price.toMoney(),
            )

            // when
            val result = productService.getProducts(ProductSearchCondition())

            // then
            result shouldHaveSize 1
            result.first().shouldBeEqualToIgnoringFields(
                expected,
                ProductResponseDto::id,
            )
        }
    }
})