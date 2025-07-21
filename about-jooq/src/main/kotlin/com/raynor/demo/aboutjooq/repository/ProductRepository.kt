package com.raynor.demo.aboutjooq.repository

import com.raynor.demo.aboutjooq.entity.tables.Product
import com.raynor.demo.aboutjooq.entity.tables.records.ProductRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class ProductRepository(
    private val dSLContext: DSLContext,
) {

    fun findById(id: Int): ProductRecord? {
        val product = Product.PRODUCT

        return dSLContext
            .selectFrom(product)
            .where(product.ID.eq(id))
            .fetchOne()
    }

    fun findAll(): List<ProductRecord> {
        val product = Product.PRODUCT

        return dSLContext
            .selectFrom(product)
            .fetch()
    }
}