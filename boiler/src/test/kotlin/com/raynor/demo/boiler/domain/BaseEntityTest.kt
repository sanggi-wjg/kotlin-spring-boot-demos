package com.raynor.demo.boiler.domain

import com.raynor.demo.boiler.repository.ProductRepository
import com.raynor.demo.boiler.support.ServiceTestContext
import com.raynor.demo.boiler.support.fixture.ProductFixture
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class BaseEntityTest(
    private val productRepository: ProductRepository,
) : ServiceTestContext(
        {
            test("저장 시 createdAt/updatedAt 이 채워지고 deletedAt 은 null 이다") {
                val saved = productRepository.saveAndFlush(ProductFixture.general())

                saved.createdAt.shouldNotBeNull()
                saved.updatedAt.shouldNotBeNull()
                saved.deletedAt shouldBe null
                saved.isDeleted shouldBe false
            }

            test("수정 시 updatedAt 만 갱신된다") {
                val saved = productRepository.saveAndFlush(ProductFixture.general())
                val createdAt = saved.createdAt
                val updatedAt = saved.updatedAt

                saved.delete()
                val updated = productRepository.saveAndFlush(saved)

                updated.createdAt shouldBe createdAt
                updated.updatedAt shouldNotBe updatedAt
                updated.deletedAt.shouldNotBeNull()
                updated.isDeleted shouldBe true
            }
        },
    )
