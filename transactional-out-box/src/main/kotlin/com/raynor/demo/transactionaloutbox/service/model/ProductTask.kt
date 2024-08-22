package com.raynor.demo.transactionaloutbox.service.model

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import com.raynor.demo.transactionaloutbox.enums.AggregateType
import java.time.Instant

object ProductTask {
    fun productUpdated(productId: Int): OutboxEntity {
        return OutboxEntity(
            aggregateId = productId,
            aggregateType = AggregateType.PRODUCT_UPDATED,
            payload = mapOf(
                "id" to productId,
            ),
            status = false,
            createdAt = Instant.now()
        )
    }
}