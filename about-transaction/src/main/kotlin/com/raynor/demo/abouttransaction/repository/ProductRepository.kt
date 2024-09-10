package com.raynor.demo.abouttransaction.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.abouttransaction.entity.*
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<ProductEntity, Int>, ProductQueryDSLRepository {
}

interface ProductQueryDSLRepository {
    fun findAllWithRelated(): List<ProductEntity>
}

class ProductQueryDSLRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProductQueryDSLRepository {

    private val product = QProductEntity.productEntity
    private val productOption = QProductOptionEntity.productOptionEntity
    private val productCategoryMapping = QProductCategoryMappingEntity.productCategoryMappingEntity
    private val category = QCategoryEntity.categoryEntity

    override fun findAllWithRelated(): List<ProductEntity> {
        return jpaQueryFactory
            .select(product)
            .from(product)
            .innerJoin(productOption).on(productOption.product.id.eq(product.id))
            .innerJoin(productCategoryMapping).on(productCategoryMapping.product.id.eq(product.id))
            .innerJoin(category).on(category.id.eq(productCategoryMapping.category.id))
            .where()
            .fetch()
    }
}
