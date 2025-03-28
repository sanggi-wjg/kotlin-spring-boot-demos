package com.raynor.demo.transactionaloutbox.service

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import com.raynor.demo.transactionaloutbox.repository.OutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class TaskService(
    private val outboxRepository: OutboxRepository,
) {

    fun publishOnUserSigned(userId: Int): OutboxEntity {
        return outboxRepository.save(
            OutboxEntity.userSigned(userId)
        )
    }

    fun publishOnProductUpdated(productId: Int): OutboxEntity {
        return outboxRepository.save(
            OutboxEntity.productUpdated(productId)
        )
    }
}
