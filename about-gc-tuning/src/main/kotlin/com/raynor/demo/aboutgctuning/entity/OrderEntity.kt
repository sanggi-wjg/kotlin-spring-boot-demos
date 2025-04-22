package com.raynor.demo.aboutgctuning.entity

import com.raynor.demo.aboutgctuning.enum.OrderStatus
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "orders", schema = "tuning")
class OrderEntity(
    orderNumber: String,
    amount: BigDecimal,
    quantity: Int,
    createdAt: Instant,
    product: ProductEntity
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        private set

    @NotNull
    @Column(name = "order_number", nullable = false)
    var orderNumber: String = orderNumber
        private set

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 0)
    var amount: BigDecimal = amount
        private set

    @NotNull
    @Column(name = "quantity", nullable = false)
    var quantity: Int = quantity
        private set

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status", nullable = false)
    var status: OrderStatus = OrderStatus.NEW_ORDER
        private set

    @NotNull
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = createdAt
        private set

    @Column(name = "completed_at")
    var completedAt: Instant? = null
        private set

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity = product
        private set

    fun isBeforeComplete(): Boolean {
        return this.status in listOf(
            OrderStatus.NEW_ORDER,
            OrderStatus.SHIPPING,
            OrderStatus.DELIVERED,
        )
    }

    fun complete() {
        require(this.isBeforeComplete()) {
            "Order is already completed"
        }

        this.status = OrderStatus.COMPLETED
        this.completedAt = Instant.now()
    }
}