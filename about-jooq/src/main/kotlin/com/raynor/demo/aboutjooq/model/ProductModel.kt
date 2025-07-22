package com.raynor.demo.aboutjooq.model

import com.raynor.demo.aboutjooq.entity.tables.records.ProductRecord
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset

data class ProductModel(
    val id: Int,
    val name: String,
    val memo: String?,
    val price: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(record: ProductRecord): ProductModel {
            return ProductModel(
                id = record.id!!,
                name = record.name!!,
                memo = record.memo,
                price = record.price!!,
                createdAt = record.createdAt!!.toInstant(ZoneOffset.UTC),
                updatedAt = record.updatedAt!!.toInstant(ZoneOffset.UTC),
            )
        }
    }
}