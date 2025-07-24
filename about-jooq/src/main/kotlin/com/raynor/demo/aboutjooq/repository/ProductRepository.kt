package com.raynor.demo.aboutjooq.repository

import com.raynor.demo.aboutjooq.entity.tables.Product
import com.raynor.demo.aboutjooq.entity.tables.records.ProductRecord
import com.raynor.demo.aboutjooq.model.ProductCreate
import com.raynor.demo.aboutjooq.model.ProductUpdate
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ProductRepository(
    private val dslContext: DSLContext,
) {
    private val product = Product.PRODUCT

    fun findById(id: Int): ProductRecord? {
        return dslContext
            .selectFrom(product)
            .where(product.ID.eq(id))
            .fetchOne()
    }

    fun findAll(): List<ProductRecord> {
        return dslContext
            .selectFrom(product)
            .orderBy(product.ID.desc())
            .fetch()
    }

    fun saveWithMultipleInsert(products: List<ProductRecord>): IntArray {
        if (products.isEmpty()) return intArrayOf()

        return dslContext.batchInsert(products).execute()
    }

    fun saveWithValues(products: List<ProductCreate>): Int {
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
                LocalDateTime.now(),
                LocalDateTime.now(),
            )
        }

        return insertStep.execute()
    }

    fun save(productCreate: ProductCreate): ProductRecord {
        return dslContext
            .insertInto(product)
            .set(product.NAME, productCreate.name)
            .set(product.MEMO, productCreate.memo)
            .set(product.PRICE, productCreate.price)
            .set(product.CREATED_AT, LocalDateTime.now())
            .set(product.UPDATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne()!!
    }

    fun update(id: Int, productUpdate: ProductUpdate): Boolean {
        return dslContext
            .update(product)
            .set(product.NAME, productUpdate.name)
            .set(product.MEMO, productUpdate.memo)
            .set(product.PRICE, productUpdate.price)
            .where(product.ID.eq(id))
            .returning()
            .execute() == 1
    }

    fun delete(id: Int): Boolean {
        return dslContext
            .deleteFrom(product)
            .where(product.ID.eq(id))
            .execute() == 1
    }
}