package com.raynor.demo.batchbulkexcel.storage.rds.entity

import com.raynor.demo.batchbulkexcel.storage.rds.enum.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "orders")
class OrderEntity(
    userId: Long,
    totalPrice: Long,
    status: OrderStatus,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
        private set

    @Column(name = "user_id", nullable = false)
    var userId: Long = userId
        private set

    @Column(name = "total_price", nullable = false)
    var totalPrice: Long = totalPrice
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = status
        private set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null
        private set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
        private set
}
