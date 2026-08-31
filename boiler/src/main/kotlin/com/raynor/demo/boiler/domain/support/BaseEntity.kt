package com.raynor.demo.boiler.domain.support

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
        protected set

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
        protected set

    val isDeleted: Boolean
        get() = deletedAt != null

    fun delete(deletedAt: Instant = Instant.now()) {
        this.deletedAt = deletedAt
    }

    fun restore() {
        this.deletedAt = null
    }
}
