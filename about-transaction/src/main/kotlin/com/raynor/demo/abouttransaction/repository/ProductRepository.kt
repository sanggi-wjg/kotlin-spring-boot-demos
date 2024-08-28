package com.raynor.demo.abouttransaction.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.abouttransaction.entity.ProductEntity
import com.raynor.demo.abouttransaction.entity.QProductEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository :
    JpaRepository<ProductEntity, Int>,
    ProductQueryDSLRepository {

}

interface ProductQueryDSLRepository {
    fun findAllWithRelated(): List<ProductEntity>
}

class ProductQueryDSLRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProductQueryDSLRepository {

    private val product = QProductEntity.productEntity

    override fun findAllWithRelated(): List<ProductEntity> {
        return jpaQueryFactory
            .select(product)
            .from(product)
            .where()
            .fetch()
    }
}
