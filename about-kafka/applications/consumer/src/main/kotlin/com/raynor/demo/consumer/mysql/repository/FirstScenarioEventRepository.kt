package com.raynor.demo.consumer.mysql.repository

import com.raynor.demo.consumer.mysql.entity.FirstScenarioEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FirstScenarioEventRepository : JpaRepository<FirstScenarioEventEntity, Long>