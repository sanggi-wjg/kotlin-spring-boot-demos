package com.raynor.demo.batchbulkexcel.api.service

import com.raynor.demo.batchbulkexcel.api.controller.dto.ExportRequestDto
import com.raynor.demo.batchbulkexcel.storage.rds.entity.ExcelRequestEntity
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import com.raynor.demo.batchbulkexcel.storage.rds.repository.ExcelRequestRepository
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class ExcelExportService(
    private val repository: ExcelRequestRepository,
    private val objectMapper: ObjectMapper,
) {
    fun export(request: ExportRequestDto): String {
        val jobId = UUID.randomUUID().toString()
        repository.save(
            ExcelRequestEntity(
                id = jobId,
                requestType = ExcelRequestType.EXPORT_ORDER,
                status = ExcelRequestStatus.PENDING,
                params = objectMapper.writeValueAsString(request),
            ),
        )
        return jobId
    }
}
