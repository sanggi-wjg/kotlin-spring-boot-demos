package com.raynor.demo.mysql.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
@Table(name = "user", schema = "security")
class UserEntity(
    name: String,
    email: String,
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
    @Column(name = "email", nullable = false)
    var email: String = email
        private set
}