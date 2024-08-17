package com.raynor.demo.transactionaloutbox.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.jetbrains.annotations.NotNull
import java.time.Instant

@Table(name = "outbox")
@Entity
class OutboxEntity(
    aggregateId: Int,
    aggregateType: String,
    eventType: String,
    payload: Map<String, Any>,
    isPublished: Boolean,
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
//    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false)
    var aggregateType: String = aggregateType
        private set

    @NotNull
//    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    var eventType: String = eventType
        private set

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    var payload: Map<String, Any> = payload
        private set

    @NotNull
    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = isPublished
        private set

    @NotNull
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = createdAt
        private set
}
