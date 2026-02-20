package com.raynor.demo.springbatchargoworkflows.controller

import com.raynor.demo.springbatchargoworkflows.config.JobOperatorResolver
import com.raynor.demo.springbatchargoworkflows.controller.dto.JobExecutionResponseDto
import com.raynor.demo.springbatchargoworkflows.controller.dto.JobLaunchRequestDto
import com.raynor.demo.springbatchargoworkflows.controller.dto.JobLaunchResponseDto
import com.raynor.demo.springbatchargoworkflows.controller.dto.JobRestartResponseDto
import com.raynor.demo.springbatchargoworkflows.exceptions.JobExecutionNotFoundException
import com.raynor.demo.springbatchargoworkflows.exceptions.JobExecutionStillRunningException
import com.raynor.demo.springbatchargoworkflows.exceptions.JobNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobExecutionNotRunningException
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
        @RequestBody request: JobLaunchRequestDto,
    ): ResponseEntity<JobLaunchResponseDto> {
        val job = jobs[jobName]
            ?: throw JobNotFoundException(jobName)

        val jobParameters = JobParametersBuilder()
            .addString("period", request.period.name)
            .addString("execAt", request.period.toParameterValue())
            .also {
                request.params?.forEach { (key, value) ->
                    it.addString(key, value)
                }
            }
            .toJobParameters()
        log.info("🚀 [{}] Job 실행 요청. parameters={}", jobName, jobParameters)

        val operator = jobOperatorResolver.resolve(jobName)
        val execution = operator.start(job, jobParameters)

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
            JobLaunchResponseDto(
                executionId = execution.id,
                jobName = jobName,
                status = execution.status.name,
            )
        )
    }

    @GetMapping("/jobs/executions/{executionId}")
    fun getExecution(
        @PathVariable executionId: Long,
    ): ResponseEntity<JobExecutionResponseDto> {
        val execution = jobRepository.getJobExecution(executionId)
            ?: throw JobExecutionNotFoundException(executionId)

        val failedSteps = execution.stepExecutions
            .filter { it.status == BatchStatus.FAILED }
            .map { it.stepName }

        log.info(
            "📊 [{}] Job 실행 상태 조회. executionId={}, status={}, failedSteps={}",
            execution.jobInstance.jobName,
            executionId,
            execution.status.name,
            failedSteps
        )

        val dto = JobExecutionResponseDto(
            executionId = execution.id,
            jobName = execution.jobInstance.jobName,
            status = execution.status.name,
            exitCode = execution.exitStatus.exitCode,
            exitDescription = execution.exitStatus.exitDescription,
            startTime = execution.startTime,
            endTime = execution.endTime,
            failedSteps = failedSteps,
        )

        val httpStatus = if (execution.isRunning) HttpStatus.ACCEPTED else HttpStatus.OK
        return ResponseEntity.status(httpStatus).body(dto)
    }

    @PostMapping("/jobs/executions/{executionId}/stop")
    fun stopExecution(
        @PathVariable executionId: Long,
    ): ResponseEntity<Map<String, String>> {
        val execution = jobRepository.getJobExecution(executionId)
            ?: throw JobExecutionNotFoundException(executionId)

        if (!execution.isRunning) {
            throw JobExecutionNotRunningException("Execution is not running. status=${execution.status}")
        }

        val operator = jobOperatorResolver.resolve(execution.jobInstance.jobName)
        operator.stop(execution)
        log.info("🛑 [{}] Job 중지 요청. executionId={}", execution.jobInstance.jobName, executionId)

        return ResponseEntity.ok(mapOf("message" to "Stop requested for execution $executionId"))
    }

    @PostMapping("/jobs/executions/{executionId}/restart")
    fun restartExecution(
        @PathVariable executionId: Long,
    ): ResponseEntity<JobRestartResponseDto> {
        val execution = jobRepository.getJobExecution(executionId)
            ?: throw JobExecutionNotFoundException(executionId)

        if (execution.isRunning) {
            throw JobExecutionStillRunningException(execution.status)
        }

        val operator = jobOperatorResolver.resolve(execution.jobInstance.jobName)
        val newExecution = operator.restart(execution)
        log.info("🔄 [{}] Job 재시작. originalId={}, newId={}", execution.jobInstance.jobName, executionId, newExecution.id)

        return ResponseEntity.ok(
            JobRestartResponseDto(
                originalExecutionId = executionId,
                newExecutionId = newExecution.id,
                jobName = execution.jobInstance.jobName,
                status = newExecution.status.name,
            )
        )
    }
}