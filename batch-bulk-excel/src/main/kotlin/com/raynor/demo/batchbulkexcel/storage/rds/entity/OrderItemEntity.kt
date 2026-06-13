package com.raynor.demo.batchbulkexcel.storage.rds.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name = "order_item",
    indexes = [Index(name = "idx_order", columnList = "order_id")],
)
class OrderItemEntity(
    orderId: Long,
    productName: String,
    quantity: Int,
    price: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
        private set

    @Column(name = "order_id", nullable = false)
    var orderId: Long = orderId
        private set

    @Column(name = "product_name", nullable = false, length = 255)
    var productName: String = productName
        private set

    @Column(nullable = false)
    var quantity: Int = quantity
        private set

    @Column(nullable = false)
    var price: Long = price
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
