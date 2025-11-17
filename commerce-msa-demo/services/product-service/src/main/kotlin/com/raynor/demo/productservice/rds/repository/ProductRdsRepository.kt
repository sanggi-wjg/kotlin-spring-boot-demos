package com.raynor.demo.productservice.rds.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.productservice.rds.entity.ProductEntity
import com.raynor.demo.productservice.rds.entity.QProductEntity
import com.raynor.demo.productservice.service.model.query.ProductSearchQuery
import com.raynor.demo.productservice.service.model.query.SortDirection
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRdsRepository : JpaRepository<ProductEntity, Long>, ProductQueryDslRepository

interface ProductQueryDslRepository {
    fun findPageByQuery(searchQuery: ProductSearchQuery): List<ProductEntity>

    fun findByIdWithLock(productId: Long): ProductEntity?
}

class ProductQueryDslRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProductQueryDslRepository {

    private val product = QProductEntity.productEntity

    override fun findPageByQuery(searchQuery: ProductSearchQuery): List<ProductEntity> {
        val jpaQuery = jpaQueryFactory.selectFrom(product)

        searchQuery.cursorId?.let { lastId ->
            when (searchQuery.sortDirection) {
                SortDirection.ASC -> jpaQuery.where(product.id.gt(lastId.value))
                SortDirection.DESC -> jpaQuery.where(product.id.lt(lastId.value))
            }
        }

        val orderSpec = when (searchQuery.sortDirection) {
            SortDirection.ASC -> product.id.asc()
            SortDirection.DESC -> product.id.desc()
        }

        return jpaQuery
            .orderBy(orderSpec)
            .limit(searchQuery.size)
            .fetch()
    }

    override fun findByIdWithLock(productId: Long): ProductEntity? {
        return jpaQueryFactory
            .selectFrom(product)
            .where(product.id.eq(productId))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne()
    }
}
