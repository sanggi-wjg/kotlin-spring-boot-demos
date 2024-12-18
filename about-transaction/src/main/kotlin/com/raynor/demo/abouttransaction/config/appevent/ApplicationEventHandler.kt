package com.raynor.demo.abouttransaction.config.appevent

import com.raynor.demo.abouttransaction.entity.CategoryEntity
import com.raynor.demo.abouttransaction.entity.ProductCategoryMapEntity
import com.raynor.demo.abouttransaction.entity.ProductEntity
import com.raynor.demo.abouttransaction.entity.ProductOptionEntity
import com.raynor.demo.abouttransaction.repository.CategoryRepository
import com.raynor.demo.abouttransaction.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ApplicationEventHandler(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @EventListener(ApplicationStartedEvent::class)
    fun onApplicationStartedEvent(event: ApplicationStartedEvent) {
        logger.info("[APP_STATED_EVENT_0] START")
        createProduct1()
        createProduct2()
        logger.info("[APP_STATED_EVENT_99] END")
    }

    private fun createProduct1() {
        val category = categoryRepository.save(
            CategoryEntity(name = "로션")
        )
        productRepository.save(
            ProductEntity(
                name = "일리윤 세라마이드",
            ).apply {
                this.addProductOptions(
                    listOf(
                        ProductOptionEntity(name = "일리윤 세라마이드 500ml", price = 5_000.toBigDecimal(), product = this),
                        ProductOptionEntity(name = "일리윤 세라마이드 1000ml", price = 9_000.toBigDecimal(), product = this),
                        ProductOptionEntity(name = "일리윤 세라마이드 2000ml", price = 18_000.toBigDecimal(), product = this),
                    )
                )
                this.addProductCategoryMappings(
                    listOf(
                        ProductCategoryMapEntity(category = category, product = this),
                    )
                )
            }
        )
    }

    private fun createProduct2() {
        val categories = categoryRepository.saveAll(
            listOf(
                CategoryEntity(name = "애플"),
                CategoryEntity(name = "전자기기"),
                CategoryEntity(name = "노트북"),
            )
        )

        productRepository.save(
            ProductEntity(
                name = "Mac M1",
            ).apply {
                this.addProductOptions(
                    listOf(
                        ProductOptionEntity(name = "M1 Vanilla", price = 999.toBigDecimal(), product = this),
                        ProductOptionEntity(name = "M1 Prod", price = 1_999.toBigDecimal(), product = this),
                        ProductOptionEntity(name = "M1 Ultra", price = 3_999.toBigDecimal(), product = this),
                    )
                )
                this.addProductCategoryMappings(
                    categories.map {
                        ProductCategoryMapEntity(category = it, product = this)
                    }
                )
            }
        )
    }
}