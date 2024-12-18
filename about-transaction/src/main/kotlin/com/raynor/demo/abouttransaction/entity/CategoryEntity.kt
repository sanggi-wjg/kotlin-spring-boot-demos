package com.raynor.demo.abouttransaction.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
@Table(name = "category")
class CategoryEntity(
    name: String
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

    fun updateId(id: Int) {
        this.id = id
    }
}