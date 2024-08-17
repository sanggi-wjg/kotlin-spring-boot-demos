package com.raynor.demo.transactionaloutbox.repository

import com.raynor.demo.transactionaloutbox.entity.OutboxEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxRepository : JpaRepository<OutboxEntity, Long> {

}
