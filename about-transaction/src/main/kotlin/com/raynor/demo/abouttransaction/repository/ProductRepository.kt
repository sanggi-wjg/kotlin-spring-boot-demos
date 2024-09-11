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
        // 아래는 이 에러 발생함, MultipleBagFetchException: cannot simultaneously fetch multiple bags
//        .join(product.mutableProductOptions, productOption).fetchJoin()
//        .join(product.mutableProductCategoryMappings, productCategoryMapping).fetchJoin()
//        .join(productCategoryMapping.category, category).fetchJoin()

        return jpaQueryFactory
            .select(product)
            .from(product)
            .join(productOption).on(productOption.product.id.eq(product.id))
            .join(productCategoryMapping).on(productCategoryMapping.product.id.eq(product.id))
            .join(category).on(category.id.eq(productCategoryMapping.category.id))
            .where()
            .fetch()
    }
}
