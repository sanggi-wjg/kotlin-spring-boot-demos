package com.raynor.demo.batchbulkexcel.api.controller

import com.raynor.demo.batchbulkexcel.api.controller.dto.JobDetailResponseDto
import com.raynor.demo.batchbulkexcel.api.controller.dto.JobListResponseDto
import com.raynor.demo.batchbulkexcel.api.service.ExcelJobQueryService
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class ExcelJobController(
    private val service: ExcelJobQueryService,
) {
    @GetMapping("/excel/jobs/{jobId}")
    fun getJob(
        @PathVariable jobId: String,
    ): ResponseEntity<JobDetailResponseDto> = ResponseEntity.ok(service.getJob(jobId))

    @GetMapping("/excel/jobs")
    fun listJobs(
        @RequestParam(required = false) type: ExcelRequestType?,
        @RequestParam(required = false) status: ExcelRequestStatus?,
        @RequestParam(required = false) from: Instant?,
        @RequestParam(required = false) to: Instant?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<JobListResponseDto> = ResponseEntity.ok(service.listJobs(type, status, from, to, page, size))
}
