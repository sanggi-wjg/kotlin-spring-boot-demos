package com.raynor.demo.batchbulkexcel.storage.rds.entity

import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "excel_request",
    indexes = [Index(name = "idx_status_created", columnList = "status, created_at")],
)
class ExcelRequestEntity(
    id: String = UUID.randomUUID().toString(),
    requestType: ExcelRequestType,
    status: ExcelRequestStatus = ExcelRequestStatus.PENDING,
    params: String? = null,
    inputFileUrl: String? = null,
    resultFileUrl: String? = null,
    errorReportUrl: String? = null,
    resultSummary: String? = null,
    batchJobExecutionId: Long? = null,
    startedAt: Instant? = null,
    finishedAt: Instant? = null,
) {
    @Id
    @Column(length = 36)
    var id: String = id
        private set

    @Enumerated(EnumType.STRING)
    @Column(name = "excel_request_type", nullable = false, length = 50)
    var requestType: ExcelRequestType = requestType
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ExcelRequestStatus = status
        private set

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    var params: String? = params
        private set

    @Column(name = "input_file_url", length = 512)
    var inputFileUrl: String? = inputFileUrl
        private set

    @Column(name = "result_file_url", length = 512)
    var resultFileUrl: String? = resultFileUrl
        private set

    @Column(name = "error_report_url", length = 512)
    var errorReportUrl: String? = errorReportUrl
        private set

    @Column(name = "result_summary", columnDefinition = "json")
    var resultSummary: String? = resultSummary
        private set

    @Column(name = "batch_job_execution_id")
    var batchJobExecutionId: Long? = batchJobExecutionId
        private set

    @Column(name = "started_at")
    var startedAt: Instant? = startedAt
        private set

    @Column(name = "finished_at")
    var finishedAt: Instant? = finishedAt
        private set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null
        private set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
        private set
}
