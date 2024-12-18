package com.raynor.demo.abouttransaction.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull


@Entity
@Table(name = "product_category_map")
class ProductCategoryMapEntity(
    product: ProductEntity,
    category: CategoryEntity
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        private set

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity = product
        private set

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: CategoryEntity = category
        private set
}