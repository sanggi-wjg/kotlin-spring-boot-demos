package com.raynor.demo.abouttransaction.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
@Table(name = "product")
class ProductEntity(
    name: String,
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

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    private var mutableProductOptions: MutableList<ProductOptionEntity> = mutableListOf()
    val productOptions: Set<ProductOptionEntity>
        get() = mutableProductOptions.toSet()

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    private var mutableProductCategoryMappings: MutableList<ProductCategoryMappingEntity> = mutableListOf()
    val productCategoryMappings: Set<ProductCategoryMappingEntity>
        get() = mutableProductCategoryMappings.toSet()

    fun addProductOptions(productOptions: List<ProductOptionEntity>) {
        mutableProductOptions.addAll(productOptions)
    }

    fun addProductCategoryMappings(productCategoryMappings: List<ProductCategoryMappingEntity>) {
        mutableProductCategoryMappings.addAll(productCategoryMappings)
    }
}
