package com.raynor.demo.boiler.domain.product

import com.raynor.demo.boiler.domain.support.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "product")
open class Product(
    name: String,
    price: BigDecimal,
    stockQuantity: Long = 0L,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        protected set

    @Column(name = "name", nullable = false, length = 64)
    var name: String = name
        protected set

    @Column(name = "price", nullable = false, precision = 15, scale = 0)
    var price: BigDecimal = price
        protected set

    @Column(name = "stock_quantity", nullable = false)
    var stockQuantity: Long = stockQuantity
        protected set

    fun getStockStatus(): ProductStockStatus {
        return if (stockQuantity > 0) {
            ProductStockStatus.IN_STOCK
        } else {
            ProductStockStatus.OUT_OF_STOCK
        }
    }

    fun isSale(): Boolean {
        return getStockStatus() == ProductStockStatus.IN_STOCK
    }
}
