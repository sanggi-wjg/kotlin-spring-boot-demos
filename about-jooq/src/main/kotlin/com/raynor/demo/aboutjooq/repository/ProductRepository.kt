package com.raynor.demo.aboutjooq.repository

import com.raynor.demo.aboutjooq.entity.tables.Product
import com.raynor.demo.aboutjooq.entity.tables.records.ProductRecord
import com.raynor.demo.aboutjooq.model.ProductCreate
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class ProductRepository(
    private val dslContext: DSLContext,
) {

    fun findById(id: Int): ProductRecord? {
        val product = Product.PRODUCT

        return dslContext
            .selectFrom(product)
            .where(product.ID.eq(id))
            .fetchOne()
    }

    fun findAll(): List<ProductRecord> {
        val product = Product.PRODUCT

        return dslContext
            .selectFrom(product)
            .fetch()
    }

    fun saveWithMultipleInsert(products: List<ProductRecord>): IntArray {
        return dslContext.batchInsert(products).execute()
    }

    fun saveWithValues(products: List<ProductCreate>): Int {
        val product = Product.PRODUCT
        if (products.isEmpty()) return 0

        val insertStep = dslContext
            .insertInto(product)
            .columns(
                product.NAME,
                product.MEMO,
                product.PRICE,
                product.CREATED_AT,
                product.UPDATED_AT,
            )

        products.forEach { item ->
            insertStep.values(
                item.name,
                item.memo,
                item.price,
                item.createdAt,
                item.updatedAt
            )
        }

        return insertStep.execute()
    }
}