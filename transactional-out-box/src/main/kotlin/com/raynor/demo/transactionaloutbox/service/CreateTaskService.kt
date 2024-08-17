package com.raynor.demo.transactionaloutbox.service

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import com.raynor.demo.transactionaloutbox.repository.OutboxRepository
import com.raynor.demo.transactionaloutbox.service.model.UserTask
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CreateTaskService(
    private val outboxRepository: OutboxRepository,
) {
    fun createTaskOnUserSigned(): OutboxEntity {
        val uuid = UUID.randomUUID().toString()
        return UserTask.userSigned(uuid, "$uuid@dev.com").let {
            outboxRepository.save(it)
        }
    }
}
