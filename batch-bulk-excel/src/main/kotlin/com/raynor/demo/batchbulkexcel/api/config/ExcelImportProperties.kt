package com.raynor.demo.batchbulkexcel.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.unit.DataSize

@ConfigurationProperties("app.excel.import")
data class ExcelImportProperties(
    val maxFileSize: DataSize,
    val allowedExtension: String,
)
