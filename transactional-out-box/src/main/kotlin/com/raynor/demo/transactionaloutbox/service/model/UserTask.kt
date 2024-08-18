package com.raynor.demo.transactionaloutbox.service.model

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import java.time.Instant

object UserTask {
    fun userSigned(userId: Int, name: String, email: String): OutboxEntity {
        return OutboxEntity(
            aggregateId = userId,
            aggregateType = "USER",
            eventType = "USER_SIGNED",
            payload = mapOf(
                "id" to userId,
                "name" to name,
                "email" to email
            ),
            status = false,
            createdAt = Instant.now()
        )
    }
}
