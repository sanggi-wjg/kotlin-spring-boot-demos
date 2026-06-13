package com.raynor.demo.batchbulkexcel.storage.rds.repository

import com.raynor.demo.batchbulkexcel.storage.rds.entity.ExcelRequestEntity
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ExcelRequestRepository : JpaRepository<ExcelRequestEntity, String> {
    @Query(
        """
        SELECT e FROM ExcelRequestEntity e
        WHERE (:type IS NULL OR e.requestType = :type)
          AND (:status IS NULL OR e.status = :status)
          AND (:from IS NULL OR e.createdAt >= :from)
          AND (:to IS NULL OR e.createdAt <= :to)
        """,
    )
    fun search(
        @Param("type") type: ExcelRequestType?,
        @Param("status") status: ExcelRequestStatus?,
        @Param("from") from: Instant?,
        @Param("to") to: Instant?,
        pageable: Pageable,
    ): Page<ExcelRequestEntity>
}
