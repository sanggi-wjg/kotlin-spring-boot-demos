package com.raynor.demo.batchbulkexcel.batch.controller.dto

import com.raynor.demo.batchbulkexcel.batch.enum.JobLaunchPeriod

data class JobLaunchRequestDto(
    val period: JobLaunchPeriod,
    val params: Map<String, String>? = null,
)
