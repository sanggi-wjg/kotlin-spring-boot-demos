package com.raynor.demo.boiler.domain.product

import com.raynor.demo.boiler.domain.support.BaseEntity
import com.raynor.demo.boiler.domain.support.Money
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "product")
open class Product(
    name: String,
    price: Money,
    status: ProductStatus = ProductStatus.ON_SALE,
    stock: Stock = Stock(0L),
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        protected set

    @Column(name = "name", nullable = false, length = 64)
    var name: String = name
        protected set

    @Embedded
    @AttributeOverride(name = "amount", column = Column(name = "price", nullable = false, precision = 15, scale = 0))
    var price: Money = price
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 64)
    var status: ProductStatus = status
        protected set

    @Embedded
    var stock: Stock = stock
        protected set

    fun isStockStatusOutOfStock(): Boolean {
        return this.stock.getStatus() == StockStatus.OUT_OF_STOCK
    }

    fun isSale(): Boolean {
        return this.stock.getStatus() == StockStatus.IN_STOCK && this.status == ProductStatus.ON_SALE
    }
}
