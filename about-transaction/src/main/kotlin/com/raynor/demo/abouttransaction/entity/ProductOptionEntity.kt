package com.raynor.demo.abouttransaction.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal

@Entity
@Table(name = "product_option")
class ProductOptionEntity(
    name: String,
    price: BigDecimal,
    product: ProductEntity
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        protected set

    @Column(name = "name")
    var name: String = name
        protected set

    @NotNull
    @Column(name = "price", nullable = false)
    var price: BigDecimal = price
        protected set

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity = product
        protected set
}