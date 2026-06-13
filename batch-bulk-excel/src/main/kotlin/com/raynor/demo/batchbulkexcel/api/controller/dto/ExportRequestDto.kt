package com.raynor.demo.batchbulkexcel.api.controller.dto

import com.raynor.demo.batchbulkexcel.storage.rds.enum.OrderStatus

data class ExportRequestDto(
    val status: OrderStatus? = null,
)
