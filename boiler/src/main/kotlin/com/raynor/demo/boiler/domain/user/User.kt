package com.raynor.demo.boiler.domain.user

import com.raynor.demo.boiler.domain.support.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
open class User(
    name: String,
    isAdmin: Boolean = false,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        protected set

    @Column(name = "name", nullable = false, length = 255)
    var name: String = name
        protected set

    @Column(name = "is_admin", nullable = false)
    var isAdmin: Boolean = isAdmin
        protected set
}
