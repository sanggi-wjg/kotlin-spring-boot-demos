package com.raynor.demo.dbvendor.postgresql

import com.raynor.demo.dbvendor.enum.OrderStatus
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "order", schema = "public")
class OrderPostgreSQLEntity(
    orderNumber: String,
    amount: BigDecimal,
    createdAt: Instant,
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
    @Column(name = "amount", nullable = false)
    var amount: BigDecimal = amount
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

    fun complete(time: Instant) {
        this.status = OrderStatus.COMPLETED
        this.completedAt = time
    }
}
