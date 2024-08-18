package com.raynor.demo.transactionaloutbox.service.model

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import java.time.Instant

object ProductTask {
    fun productUpdated(productId: Int): OutboxEntity {
        return OutboxEntity(
            aggregateId = productId,
            aggregateType = "PRODUCT",
            eventType = "PRODUCT_UPDATED",
            payload = mapOf(
                "id" to productId,
            ),
            status = false,
            createdAt = Instant.now()
        )
    }
}