package com.raynor.demo.orderservice.rds.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "order", schema = "msa_order")
@EntityListeners(AuditingEntityListener::class)
class OrderItemEntity(
    productId: Long,
    quantity: Int,
    amount: BigDecimal
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "product_id")
    var productId: Long = productId
        private set

    @Column(name = "quantity")
    var quantity: Int = quantity
        private set

    @Column(name = "amount")
    var amount: BigDecimal = amount
        private set

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant
}