package com.raynor.demo.abouttransaction.model.projection

import com.querydsl.core.types.ConstructorExpression
import com.querydsl.core.types.Projections
import com.raynor.demo.abouttransaction.annotation.NoArg
import com.raynor.demo.abouttransaction.entity.*

@NoArg
data class ProductWitRelatedProjection(
    val product: ProductEntity,
    val productOption: ProductOptionEntity,
    val category: CategoryEntity
) {
    companion object {
        fun projection(): ConstructorExpression<ProductWitRelatedProjection> {
            return Projections.constructor(
                ProductWitRelatedProjection::class.java,
                QProductEntity.productEntity,
                QProductOptionEntity.productOptionEntity,
                QCategoryEntity.categoryEntity
            )
        }
    }
}
