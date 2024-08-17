package com.raynor.demo.transactionaloutbox.service

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import com.raynor.demo.transactionaloutbox.event.UserSignedEvent
import com.raynor.demo.transactionaloutbox.repository.OutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateTaskService(
    private val outboxRepository: OutboxRepository,
) {
    fun createTaskOnUserSigned(): OutboxEntity {
        return UserSignedEvent.of("Raynor", "H8s9H@example.com").let {
            outboxRepository.save(it)
        }
    }
}
