package com.raynor.demo.boiler.domain.product

import com.raynor.demo.boiler.domain.support.BaseEntity
import com.raynor.demo.boiler.domain.support.Money
import jakarta.persistence.*

@Entity
@Table(name = "product")
open class Product(
    name: String,
    price: Money,
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

    @Embedded
    var stock: Stock = stock
        protected set

    fun getStockStatus(): StockStatus {
        return stock.getStatus()
    }

    fun isSale(): Boolean {
        return getStockStatus() == StockStatus.IN_STOCK
    }
}
