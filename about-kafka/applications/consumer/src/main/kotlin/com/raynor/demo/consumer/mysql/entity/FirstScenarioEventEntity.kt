package com.raynor.demo.consumer.mysql.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "first_scenario_event")
class FirstScenarioEventEntity(
    eventId: String,
    message: String,
    timestamp: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    var eventId: String = eventId
        private set

    @Column(name = "message", nullable = false, length = 500)
    var message: String = message
        private set

    @Column(name = "timestamp", nullable = false, columnDefinition = "DATETIME(6)")
    var timestamp: Instant = timestamp
        private set

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    lateinit var createdAt: Instant
}