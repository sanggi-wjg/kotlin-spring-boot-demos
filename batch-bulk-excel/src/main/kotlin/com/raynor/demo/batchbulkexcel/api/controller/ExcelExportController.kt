package com.raynor.demo.batchbulkexcel.api.controller

import com.raynor.demo.batchbulkexcel.api.controller.dto.ExportRequestDto
import com.raynor.demo.batchbulkexcel.api.controller.dto.ExportResponseDto
import com.raynor.demo.batchbulkexcel.api.service.ExcelExportService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ExcelExportController(
    private val service: ExcelExportService,
) {
    @PostMapping("/excel/export", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun export(
        @RequestBody request: ExportRequestDto,
    ): ResponseEntity<ExportResponseDto> {
        val jobId = service.export(request)
        return ResponseEntity
            .created(URI.create("/excel/jobs/$jobId"))
            .body(ExportResponseDto(jobId = jobId))
    }
}
