package com.raynor.demo.abouttransaction.repository

import com.querydsl.core.types.ConstructorExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.abouttransaction.entity.*
import com.raynor.demo.abouttransaction.model.condition.ProductSearchCondition
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<ProductEntity, Int>, ProductQueryDSLRepository {
}

interface ProductQueryDSLRepository {
    fun findAllWithRelated(): List<ProductEntity>

    fun <T> findAllByCondition(
        searchCondition: ProductSearchCondition,
        constructor: ConstructorExpression<T>,
    ): List<T>
}

class ProductQueryDSLRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProductQueryDSLRepository {

    private val product = QProductEntity.productEntity
    private val productOption = QProductOptionEntity.productOptionEntity
    private val productCategoryMap = QProductCategoryMapEntity.productCategoryMapEntity
    private val category = QCategoryEntity.categoryEntity

    override fun findAllWithRelated(): List<ProductEntity> {
        // 아래는 이 에러 발생함, MultipleBagFetchException: cannot simultaneously fetch multiple bags
//        .join(product.mutableProductOptions, productOption).fetchJoin()
//        .join(product.mutableProductCategoryMappings, productCategoryMapping).fetchJoin()
//        .join(productCategoryMapping.category, category).fetchJoin()

        return jpaQueryFactory
            .select(product)
            .from(product)
            .join(productOption).on(productOption.product.id.eq(product.id))
            .join(productCategoryMap).on(productCategoryMap.product.id.eq(product.id))
            .join(category).on(category.id.eq(productCategoryMap.category.id))
            .where()
            .fetch()
    }

    override fun <T> findAllByCondition(
        searchCondition: ProductSearchCondition,
        constructor: ConstructorExpression<T>,
    ): List<T> {
        return jpaQueryFactory
            .select(constructor)
            .from(product)
            .innerJoin(productOption).on(productOption.product.id.eq(product.id))
            .innerJoin(productCategoryMap).on(productCategoryMap.product.id.eq(product.id))
            .innerJoin(category).on(category.id.eq(productCategoryMap.category.id))
            .fetch()
    }
}
