package com.raynor.demo.transactionaloutbox.service

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import com.raynor.demo.transactionaloutbox.repository.OutboxRepository
import com.raynor.demo.transactionaloutbox.service.model.ProductTask
import com.raynor.demo.transactionaloutbox.service.model.UserTask
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.random.Random

@Service
@Transactional
class CreateTaskService(
    private val outboxRepository: OutboxRepository,
) {
    fun publishOnUserSigned(): OutboxEntity {
        val userId = Random.nextInt(10000)
        val uuid = UUID.randomUUID().toString()
        return UserTask.userSigned(userId, uuid, "$uuid@dev.com").let {
            outboxRepository.save(it)
        }
    }

    fun publishOnProductUpdated(): OutboxEntity {
        val productId = Random.nextInt(10000)
        return ProductTask.productUpdated(productId).let {
            outboxRepository.save(it)
        }
    }
}
