package com.raynor.demo.boiler.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.boiler.domain.product.Product
import com.raynor.demo.boiler.domain.product.QProduct
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository :
    JpaRepository<Product, Int>,
    ProductQueryDslRepository

interface ProductQueryDslRepository {
    fun findAllByCursor(
        perPage: Long,
        cursorId: Int?,
    ): List<Product>
}

class ProductQueryDslRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProductQueryDslRepository {
    private val product = QProduct.product

    override fun findAllByCursor(
        perPage: Long,
        cursorId: Int?,
    ): List<Product> {
        return jpaQueryFactory
            .selectFrom(product)
            .where(
                cursorId?.let { product.id.lt(it) },
            )
            .limit(perPage)
            .orderBy(product.id.desc())
            .fetch()
    }
}
