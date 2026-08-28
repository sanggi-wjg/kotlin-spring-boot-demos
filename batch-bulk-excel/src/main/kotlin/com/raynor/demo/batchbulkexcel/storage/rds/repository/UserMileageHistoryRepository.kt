package com.raynor.demo.batchbulkexcel.storage.rds.repository

import com.raynor.demo.batchbulkexcel.storage.rds.entity.UserMileageHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserMileageHistoryRepository : JpaRepository<UserMileageHistoryEntity, Long>
