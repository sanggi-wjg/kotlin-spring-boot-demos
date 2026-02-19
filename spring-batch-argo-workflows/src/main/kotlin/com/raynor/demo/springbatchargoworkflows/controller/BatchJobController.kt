package com.raynor.demo.springbatchargoworkflows.controller

import com.raynor.demo.springbatchargoworkflows.config.JobOperatorResolver
import com.raynor.demo.springbatchargoworkflows.controller.dto.JobExecutionResponse
import com.raynor.demo.springbatchargoworkflows.controller.dto.JobLaunchResponse
import com.raynor.demo.springbatchargoworkflows.controller.dto.JobRestartResponse
import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/batch")
class BatchJobController(
    private val jobs: Map<String, Job>,
    private val jobRepository: JobRepository,
    private val jobOperatorResolver: JobOperatorResolver,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/jobs")
    fun listJobs(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(jobs.keys.sorted())
    }

    @PostMapping("/jobs/{jobName}")
    fun launchJob(
        @PathVariable jobName: String,
        @RequestBody(required = false) body: Map<String, String>?,
    ): ResponseEntity<Any> {
        val job = jobs[jobName]
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Job not found: $jobName"))

        val parametersBuilder = JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())

        body?.forEach { (key, value) ->
            parametersBuilder.addString(key, value)
        }

        val operator = jobOperatorResolver.resolve(jobName)
        val execution = operator.start(job, parametersBuilder.toJobParameters())
        log.info("🚀 [{}] Job 실행 요청. executionId={}, operator={}", jobName, execution.id, if (jobOperatorResolver.isHeavy(jobName)) "heavy" else "light")

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
            JobLaunchResponse(
                executionId = execution.id,
                jobName = jobName,
                status = execution.status.name,
            )
        )
    }

    @GetMapping("/jobs/executions/{executionId}")
    fun getExecution(
        @PathVariable executionId: Long,
    ): ResponseEntity<Any> {
        val execution = jobRepository.getJobExecution(executionId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Execution not found: $executionId"))

        val failedSteps = execution.stepExecutions
            .filter { it.status == BatchStatus.FAILED }
            .map { it.stepName }

        return ResponseEntity.ok(
            JobExecutionResponse(
                executionId = execution.id,
                jobName = execution.jobInstance.jobName,
                status = execution.status.name,
                exitCode = execution.exitStatus.exitCode,
                exitDescription = execution.exitStatus.exitDescription,
                startTime = execution.startTime,
                endTime = execution.endTime,
                failedSteps = failedSteps,
            )
        )
    }

    @PostMapping("/jobs/executions/{executionId}/stop")
    fun stopExecution(
        @PathVariable executionId: Long,
    ): ResponseEntity<Any> {
        val execution = jobRepository.getJobExecution(executionId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Execution not found: $executionId"))

        if (!execution.isRunning) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to "Execution is not running. status=${execution.status}"))
        }

        val operator = jobOperatorResolver.resolve(execution.jobInstance.jobName)
        operator.stop(execution)
        log.info("🛑 [{}] Job 중지 요청. executionId={}", execution.jobInstance.jobName, executionId)

        return ResponseEntity.ok(mapOf("message" to "Stop requested for execution $executionId"))
    }

    @PostMapping("/jobs/executions/{executionId}/restart")
    fun restartExecution(
        @PathVariable executionId: Long,
    ): ResponseEntity<Any> {
        val execution = jobRepository.getJobExecution(executionId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Execution not found: $executionId"))

        if (execution.isRunning) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to "Execution is still running. status=${execution.status}"))
        }

        val operator = jobOperatorResolver.resolve(execution.jobInstance.jobName)
        val newExecution = operator.restart(execution)
        log.info("🔄 [{}] Job 재시작. originalId={}, newId={}", execution.jobInstance.jobName, executionId, newExecution.id)

        return ResponseEntity.ok(
            JobRestartResponse(
                originalExecutionId = executionId,
                newExecutionId = newExecution.id,
                jobName = execution.jobInstance.jobName,
                status = newExecution.status.name,
            )
        )
    }
}