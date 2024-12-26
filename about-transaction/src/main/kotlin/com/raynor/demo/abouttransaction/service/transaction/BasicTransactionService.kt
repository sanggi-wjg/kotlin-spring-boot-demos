package com.raynor.demo.abouttransaction.service.transaction

import com.raynor.demo.abouttransaction.entity.ProductEntity
import com.raynor.demo.abouttransaction.entity.ProductOptionEntity
import com.raynor.demo.abouttransaction.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.random.Random

@Service
class BasicTransactionService(
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun insertWithBasicTransaction(): ProductEntity {
        return ProductEntity(name = "상품 - ${UUID.randomUUID()}").let {
            productRepository.save(it)
        }
    }

    @Transactional
    fun getAndUpdateList(): List<ProductEntity> {
        return productRepository.findAll().map {
            it.updateName(UUID.randomUUID().toString())
            it.addProductOptions(
                listOf(
                    ProductOptionEntity(
                        name = "옵션 - ${UUID.randomUUID()}",
                        price = Random.nextInt(1, 100).toBigDecimal(),
                        product = it
                    )
                )
            )
            it
        }
    }

    @Transactional
    fun getList(): List<ProductEntity> {
        return productRepository.findAll()
    }
}
