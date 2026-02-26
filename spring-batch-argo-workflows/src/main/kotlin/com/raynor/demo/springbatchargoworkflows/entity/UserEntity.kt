package com.raynor.demo.springbatchargoworkflows.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity(
    name: String,
    age: Int,
    createdAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        protected set

    @Column(name = "name", nullable = false, length = 254)
    var name: String = name
        protected set

    @Column(name = "age", nullable = false)
    var age: Int = age
        protected set

    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = createdAt
        protected set

    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = createdAt
        protected set
}
