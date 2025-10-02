package com.raynor.demo.productservice.rds.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "product", schema = "msa_product")
@EntityListeners(AuditingEntityListener::class)
class ProductEntity(
    name: String,
    price: BigDecimal,
    stockQuantity: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    var name: String = name
        private set

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = price
        private set

    @NotNull
    @Column(name = "stock_quantity", nullable = false)
    var stockQuantity: Int = stockQuantity
        private set

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant

    fun reduceStockQuantity(quantity: Int) {
        check(quantity > 0) { "Quantity must be positive" }
        check(quantity >= this.stockQuantity) { "Not enough stock quantity" }

        this.stockQuantity -= quantity
    }
}
