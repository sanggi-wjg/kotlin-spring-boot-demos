package com.raynor.demo.batchbulkexcel.batch.controller

import org.springframework.batch.core.repository.JobRepository
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/batch")
class BatchController(
    private val jobRepository: JobRepository,
) {
    // TODO(PLAN 5-1): 잡 실행 트리거 구현 중 — 컴파일 위해 임시 주석 처리
    /*
    @PostMapping("/jobs/{jobName}")
    fun runJob(
        @PathVariable jobName: String,
        @RequestBody request: JobLaunchRequestDto,
    ) {
        val jobParameters =
            JobParametersBuilder()
                .addString("period", request.period.name)
                .addString("execAt", request.period.toParameterValue())
                .also {
                    request.params?.forEach { (key, value) ->
                        it.addString(key, value)
                    }
                }.toJobParameters()

        val job = jobRepository.createJobInstance(jobName, jobParameters)
        jobRepository.createJobExecution(job)

        return ResponseEntity.ok().build()
    }
     */
}
