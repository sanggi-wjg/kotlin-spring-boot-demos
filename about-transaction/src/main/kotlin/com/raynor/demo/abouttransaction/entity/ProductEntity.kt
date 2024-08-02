package com.raynor.demo.abouttransaction.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "product")
class ProductEntity(
    id: Int?,
    name: String,
    price: BigDecimal,
    stockQuantity: Int,
    createdAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = id
        protected set

    @NotNull
    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    @NotNull
    @Column(name = "price", nullable = false)
    var price: BigDecimal = price
        protected set

    @NotNull
    @Column(name = "stock_quantity", nullable = false)
    var stockQuantity: Int = stockQuantity
        protected set

    @NotNull
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = createdAt
        protected set

    @NotNull
    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false
        protected set
}
