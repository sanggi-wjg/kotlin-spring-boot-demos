package com.raynor.demo.batchbulkexcel.storage.rds.repository

import com.raynor.demo.batchbulkexcel.storage.rds.entity.UserMileageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserMileageRepository : JpaRepository<UserMileageEntity, Long>
