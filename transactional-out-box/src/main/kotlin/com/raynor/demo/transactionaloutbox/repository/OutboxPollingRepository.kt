package com.raynor.demo.transactionaloutbox.repository

import com.raynor.demo.transactionaloutbox.entity.OutboxPollingEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxPollingRepository : JpaRepository<OutboxPollingEntity, Long> {
    fun findByStatusFalse(): List<OutboxPollingEntity>
}
