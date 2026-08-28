package com.raynor.demo.batchbulkexcel.api.controller.dto

import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import java.time.Instant

data class JobDetailResponseDto(
    val jobId: String,
    val type: ExcelRequestType,
    val status: ExcelRequestStatus,
    val summary: SummaryDto?,
    val downloadUrl: String?,
    val createdAt: Instant?,
    val finishedAt: Instant?,
)

data class SummaryDto(
    val total: Long,
    val success: Long,
    val failed: Long,
)
