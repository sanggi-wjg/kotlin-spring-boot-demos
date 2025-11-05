package com.raynor.demo.consumer.mysql.repository

import com.raynor.demo.consumer.mysql.entity.DeadLetterEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeadLetterRepository : JpaRepository<DeadLetterEntity, Long>
