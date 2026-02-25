package com.raynor.demo.springbatchargoworkflows.controller.dto

import java.time.LocalDateTime

data class JobLaunchResponseDto(
    val executionId: Long,
    val jobName: String,
    val status: String,
)

data class JobExecutionResponseDto(
    val executionId: Long,
    val jobName: String,
    val status: String,
    val exitCode: String,
    val exitDescription: String,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val failedSteps: List<String>,
)

data class JobStopResponseDto(
    val executionId: Long,
    val jobName: String,
    val status: String,
)

data class JobRestartResponseDto(
    val originalExecutionId: Long,
    val newExecutionId: Long,
    val jobName: String,
    val status: String,
)
