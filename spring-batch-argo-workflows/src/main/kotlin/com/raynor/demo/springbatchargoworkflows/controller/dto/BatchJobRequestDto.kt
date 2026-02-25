package com.raynor.demo.springbatchargoworkflows.controller.dto

import com.raynor.demo.springbatchargoworkflows.enum.JobLaunchPeriod

data class JobLaunchRequestDto(
    val period: JobLaunchPeriod,
    val params: Map<String, String>? = null,
)
