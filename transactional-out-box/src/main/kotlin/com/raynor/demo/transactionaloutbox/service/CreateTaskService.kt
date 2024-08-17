package com.raynor.demo.transactionaloutbox.service

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import com.raynor.demo.transactionaloutbox.repository.OutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class CreateTaskService(
    private val outboxRepository: OutboxRepository,
) {
    fun createTaskOnUserSigned(): OutboxEntity {
        return outboxRepository.save(
            OutboxEntity(
                aggregateId = 1,
//                aggregateType = AggregateType.USER,
//                eventType = EventType.USER_SIGNED,
                aggregateType = "USER",
                eventType = "USER_SIGNED",
                payload = mapOf(
                    "name" to "raynor",
                    "email" to "X0yQ3@example.com"
                ),
                isPublished = false,
                createdAt = Instant.now()
            )
        )
    }
}
