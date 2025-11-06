package com.raynor.demo.consumer.mysql.repository

import com.raynor.demo.consumer.mysql.entity.EventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository : JpaRepository<EventEntity, Long> {

    fun findByEventId(eventId: String): EventEntity?
}
