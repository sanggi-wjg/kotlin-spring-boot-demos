package com.raynor.demo.batchbulkexcel.storage.rds.repository

import com.raynor.demo.batchbulkexcel.storage.rds.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long>
