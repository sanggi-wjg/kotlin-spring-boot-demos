package com.raynor.demo.boiler.service.product

import com.raynor.demo.boiler.domain.product.Product
import com.raynor.demo.boiler.repository.ProductRepository
import com.raynor.demo.boiler.service.product.model.ProductModel
import com.raynor.demo.boiler.service.support.CursorSlice
import com.raynor.demo.boiler.support.ServiceTestContext
import com.raynor.demo.boiler.support.fixture.ProductFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equals.shouldBeEqual
import jakarta.persistence.EntityNotFoundException

class ProductServiceTest(
    private val productService: ProductService,
    private val productRepository: ProductRepository,
) : ServiceTestContext(
        {
            beforeEach {
                productRepository.deleteAllInBatch()
            }

            context("상품 목록 조회") {

                fun createProductFixtures(): List<Product> {
                    return productRepository.saveAll(
                        listOf(
                            ProductFixture.general(name = "1111"),
                            ProductFixture.general(name = "2222"),
                            ProductFixture.general(name = "3333"),
                        ),
                    )
                }

                test("커서 없는 경우") {
                    // given
                    val products = createProductFixtures()

                    val expected = CursorSlice(
                        hasNext = false,
                        nextCursor = products.first().id,
                        items = products.map { ProductModel.fromEntity(it) }.sortedByDescending { it.id },
                    )

                    // when
                    val result = productService.getProducts(10, null)

                    // then
                    result shouldBeEqual expected
                }

                test("커서 있는 경우") {
                    // given
                    val products = createProductFixtures()
                    val lastProduct = products.last()

                    val expected = CursorSlice(
                        hasNext = false,
                        nextCursor = products.first().id,
                        items = products.filter { it.id != lastProduct.id }
                            .map { ProductModel.fromEntity(it) }
                            .sortedByDescending { it.id },
                    )

                    // when
                    val result = productService.getProducts(10, lastProduct.id)

                    // then
                    result shouldBeEqual expected
                }
            }

            context("단건 조회") {

                test("조회 성공") {
                    // given
                    val product = productRepository.save(ProductFixture.general())

                    // when
                    val result = productService.getProduct(product.id!!)

                    // then
                    result shouldBeEqual ProductModel.fromEntity(product)
                }

                test("조회 실패") {
                    shouldThrow<EntityNotFoundException> {
                        productService.getProduct(0)
                    }
                }
            }
        },
    )
