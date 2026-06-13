package com.raynor.demo.batchbulkexcel.api.service

import com.raynor.demo.batchbulkexcel.api.controller.dto.JobDetailResponseDto
import com.raynor.demo.batchbulkexcel.api.controller.dto.JobListResponseDto
import com.raynor.demo.batchbulkexcel.api.controller.dto.JobSummaryDto
import com.raynor.demo.batchbulkexcel.api.controller.dto.SummaryDto
import com.raynor.demo.batchbulkexcel.api.exception.JobNotFoundException
import com.raynor.demo.batchbulkexcel.storage.file.FileStorage
import com.raynor.demo.batchbulkexcel.storage.rds.entity.ExcelRequestEntity
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import com.raynor.demo.batchbulkexcel.storage.rds.repository.ExcelRequestRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.time.Instant

@Service
class ExcelJobQueryService(
    private val repository: ExcelRequestRepository,
    private val fileStorage: FileStorage,
    private val objectMapper: ObjectMapper,
) {
    fun getJob(jobId: String): JobDetailResponseDto {
        val entity = repository.findById(jobId).orElseThrow { JobNotFoundException(jobId) }
        return JobDetailResponseDto(
            jobId = entity.id,
            type = entity.requestType,
            status = entity.status,
            summary = entity.resultSummary?.let { objectMapper.readValue(it, SummaryDto::class.java) },
            downloadUrl = downloadKey(entity)?.let { fileStorage.presignedUrl(it) },
            createdAt = entity.createdAt,
            finishedAt = entity.finishedAt,
        )
    }

    fun listJobs(
        type: ExcelRequestType?,
        status: ExcelRequestStatus?,
        from: Instant?,
        to: Instant?,
        page: Int,
        size: Int,
    ): JobListResponseDto {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = repository.search(type, status, from, to, pageable)
        return JobListResponseDto(
            content =
                result.content.map {
                    JobSummaryDto(
                        jobId = it.id,
                        type = it.requestType,
                        status = it.status,
                        createdAt = it.createdAt,
                        finishedAt = it.finishedAt,
                    )
                },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    private fun downloadKey(entity: ExcelRequestEntity): String? =
        when (entity.requestType) {
            ExcelRequestType.EXPORT_ORDER -> {
                entity.resultFileUrl
            }

            else -> {
                if (entity.status == ExcelRequestStatus.PARTIAL || entity.status == ExcelRequestStatus.FAILED) {
                    entity.errorReportUrl
                } else {
                    null
                }
            }
        }
}
