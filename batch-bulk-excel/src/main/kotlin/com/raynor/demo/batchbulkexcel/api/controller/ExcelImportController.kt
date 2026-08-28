package com.raynor.demo.batchbulkexcel.api.controller

import com.raynor.demo.batchbulkexcel.api.controller.dto.ImportResponseDto
import com.raynor.demo.batchbulkexcel.api.service.ExcelImportService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@RestController
class ExcelImportController(
    private val service: ExcelImportService,
) {
    @PostMapping("/excel/import", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun import(
        @RequestPart("file") file: MultipartFile,
    ): ResponseEntity<ImportResponseDto> {
        val jobId = service.import(file)
        return ResponseEntity
            .created(URI.create("/excel/jobs/$jobId"))
            .body(ImportResponseDto(jobId = jobId))
    }
}
