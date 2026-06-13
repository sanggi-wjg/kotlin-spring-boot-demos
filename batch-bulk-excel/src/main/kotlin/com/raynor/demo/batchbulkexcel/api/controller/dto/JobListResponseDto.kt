package com.raynor.demo.batchbulkexcel.api.controller.dto

import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import java.time.Instant

data class JobListResponseDto(
    val content: List<JobSummaryDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class JobSummaryDto(
    val jobId: String,
    val type: ExcelRequestType,
    val status: ExcelRequestStatus,
    val createdAt: Instant?,
    val finishedAt: Instant?,
)
