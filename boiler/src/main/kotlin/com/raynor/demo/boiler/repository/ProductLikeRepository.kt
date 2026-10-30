package com.raynor.demo.boiler.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.boiler.domain.product.ProductLike
import com.raynor.demo.boiler.domain.product.QProductLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ProductLikeRepository :
    JpaRepository<ProductLike, Long>,
    ProductLikeQueryDslRepository {
    @Modifying
    @Query(
        value = "INSERT IGNORE INTO product_like (product_id, user_id) VALUES (:productId, :userId)",
        nativeQuery = true,
    )
    fun insertIgnore(
        productId: Int,
        userId: Int,
    ): Int

    fun countByProductId(productId: Int): Long
}

interface ProductLikeQueryDslRepository {
    fun deleteByUserIdAndProductId(
        userId: Int,
        productId: Int,
    ): Long
}

class ProductLikeQueryDslRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProductLikeQueryDslRepository {

    private val productLike = QProductLike.productLike

    override fun deleteByUserIdAndProductId(
        userId: Int,
        productId: Int,
    ): Long {
        return jpaQueryFactory
            .delete(productLike)
            .where(
                productLike.user.id.eq(userId),
                productLike.product.id.eq(productId),
            )
            .execute()
    }
}
