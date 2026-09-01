package com.raynor.demo.boiler.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.boiler.domain.product.Product
import com.raynor.demo.boiler.domain.product.ProductStatus
import com.raynor.demo.boiler.domain.product.QProduct
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository :
    JpaRepository<Product, Int>,
    ProductQueryDslRepository {
    // JpaRepository 의 findById 는 soft delete 된 행도 그대로 돌려주므로 쓰지 않는다
    fun findByIdAndDeletedAtIsNull(id: Int): Product?
}

interface ProductQueryDslRepository {
    fun findAllByCursor(
        size: Long,
        cursorId: Int?,
    ): List<Product>
}

class ProductQueryDslRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProductQueryDslRepository {
    private val product = QProduct.product

    override fun findAllByCursor(
        size: Long,
        cursorId: Int?,
    ): List<Product> {
        return jpaQueryFactory
            .selectFrom(product)
            .where(
                product.deletedAt.isNull,
                product.status.eq(ProductStatus.ON_SALE),
                cursorId?.let { product.id.lt(it) },
            )
            .limit(size)
            .orderBy(product.id.desc())
            .fetch()
    }
}
