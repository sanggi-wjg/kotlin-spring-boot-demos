package com.raynor.demo.batchbulkexcel.api.service

import com.raynor.demo.batchbulkexcel.api.validator.ExcelFileValidator
import com.raynor.demo.batchbulkexcel.storage.file.FileStorage
import com.raynor.demo.batchbulkexcel.storage.rds.entity.ExcelRequestEntity
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import com.raynor.demo.batchbulkexcel.storage.rds.repository.ExcelRequestRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.UUID

@Service
class ExcelImportService(
    private val validator: ExcelFileValidator,
    private val fileStorage: FileStorage,
    private val repository: ExcelRequestRepository,
) {
    fun import(file: MultipartFile): String {
        validator.validate(file)

        val jobId = UUID.randomUUID().toString()
        val key = "excel/import/input/${LocalDate.now()}/$jobId.xlsx"
        fileStorage.store(key, file.inputStream)

        repository.save(
            ExcelRequestEntity(
                id = jobId,
                requestType = ExcelRequestType.IMPORT_USER_MILEAGE,
                status = ExcelRequestStatus.PENDING,
                inputFileUrl = key,
            ),
        )
        return jobId
    }
}
