package com.raynor.demo.productservice.rds.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.productservice.rds.entity.ProductEntity
import com.raynor.demo.productservice.rds.entity.QProductEntity
import com.raynor.demo.productservice.service.condition.ProductSearchCondition
import com.raynor.demo.productservice.service.condition.SortDirection
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRdsRepository : JpaRepository<ProductEntity, Long>, ProductQueryDslRepository

interface ProductQueryDslRepository {
    fun findPageByCondition(condition: ProductSearchCondition): List<ProductEntity>
}

class ProductQueryDslRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProductQueryDslRepository {

    private val product = QProductEntity.productEntity

    override fun findPageByCondition(condition: ProductSearchCondition): List<ProductEntity> {
        val query = jpaQueryFactory.selectFrom(product)

        condition.cursorId?.let { lastId ->
            when (condition.sortDirection) {
                SortDirection.ASC -> query.where(product.id.gt(lastId.value))
                SortDirection.DESC -> query.where(product.id.lt(lastId.value))
            }
        }

        val orderSpec = when (condition.sortDirection) {
            SortDirection.ASC -> product.id.asc()
            SortDirection.DESC -> product.id.desc()
        }

        return query
            .orderBy(orderSpec)
            .limit(condition.size)
            .fetch()
    }
}
