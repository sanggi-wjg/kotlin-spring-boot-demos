package com.raynor.demo.transactionaloutbox.event

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import java.time.Instant

class UserSignedEvent {
    companion object {
        fun of(name: String, email: String): OutboxEntity {
            return OutboxEntity(
                aggregateId = 1,
//                aggregateType = AggregateType.USER,
//                eventType = EventType.USER_SIGNED,
                aggregateType = "USER",
                eventType = "USER_SIGNED",
                payload = mapOf(
                    "name" to name,
                    "email" to email
                ),
                isPublished = false,
                createdAt = Instant.now()
            )
        }
    }
}
