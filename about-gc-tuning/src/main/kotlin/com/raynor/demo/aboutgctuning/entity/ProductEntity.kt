package com.raynor.demo.aboutgctuning.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal

@Entity
@Table(name = "product", schema = "tuning")
class ProductEntity(
    name: String,
    price: BigDecimal,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        private set

    @NotNull
    @Column(name = "name", nullable = false)
    var name: String = name
        private set

    @NotNull
    @Column(name = "price", nullable = false, precision = 15, scale = 0)
    var price: BigDecimal = price
        private set

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    var orders: List<OrderEntity> = emptyList()
        private set
}
