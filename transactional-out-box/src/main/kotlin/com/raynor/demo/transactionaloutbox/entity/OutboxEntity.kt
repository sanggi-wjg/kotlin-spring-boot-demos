package com.raynor.demo.transactionaloutbox.entity

import com.raynor.demo.transactionaloutbox.enums.AggregateType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.jetbrains.annotations.NotNull
import java.time.Instant

@Table(name = "outbox")
@Entity
class OutboxEntity(
    aggregateId: Int,
    aggregateType: AggregateType,
    payload: Map<String, Any>,
    status: Boolean,
    createdAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null
        private set

    @NotNull
    @Column(name = "aggregate_id", nullable = false)
    var aggregateId: Int = aggregateId
        private set

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false, length = 64)
    var aggregateType: AggregateType = aggregateType
        private set

//    @NotNull
////    @Enumerated(EnumType.STRING)
//    @Column(name = "event_type", nullable = false)
//    var eventType: String = eventType
//        private set

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    var payload: Map<String, Any> = payload
        private set

    @NotNull
    @Column(name = "status", nullable = false)
    var status: Boolean = status
        private set

    @NotNull
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = createdAt
        private set

    @Column(name = "completed_at")
    var completedAt: Instant? = null
        private set

    fun done() {
        this.status = true
        this.completedAt = Instant.now()
    }

    companion object {
        fun userSigned(userId: Int): OutboxEntity {
            return OutboxEntity(
                aggregateId = userId,
                aggregateType = AggregateType.USER_SIGNED,
                payload = mapOf(
                    "userId" to userId,
                ),
                status = false,
                createdAt = Instant.now()
            )
        }

        fun productUpdated(productId: Int): OutboxEntity {
            return OutboxEntity(
                aggregateId = productId,
                aggregateType = AggregateType.PRODUCT_UPDATED,
                payload = mapOf(
                    "productId" to productId,
                ),
                status = false,
                createdAt = Instant.now()
            )
        }
    }
}
