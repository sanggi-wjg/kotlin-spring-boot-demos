package com.raynor.demo.orderservice.rds.entity

import com.raynor.demo.shared.enums.OrderStatus
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "order", schema = "msa_order")
@EntityListeners(AuditingEntityListener::class)
class OrderEntity(
    productId: Long,
    quantity: Int,
    orderNumber: String,
    amount: BigDecimal,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "order_status", nullable = false)
    var orderStatus: OrderStatus = OrderStatus.PENDING
        private set

    @NotNull
    @Column(name = "product_id", nullable = false)
    var productId: Long = productId
        private set

    @NotNull
    @Column(name = "quantity", nullable = false)
    var quantity: Int = quantity
        private set

    @NotNull
    @Column(name = "order_number", nullable = false)
    var orderNumber: String = orderNumber
        private set

    @NotNull
    @Column(name = "amount", nullable = false)
    var amount: BigDecimal = amount
        private set

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant

    fun updateOrderStatus(newOrderStatus: OrderStatus) {
        this.orderStatus = newOrderStatus
    }
}
