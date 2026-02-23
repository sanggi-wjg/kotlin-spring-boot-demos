package com.raynor.demo.springbatchargoworkflows.listener

import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.listener.JobExecutionListener
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class JobCompletionListener : JobExecutionListener {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun beforeJob(jobExecution: JobExecution) {
        log.info(
            "🚀 [{}] Job 시작. executionId={}, parameters={}",
            jobExecution.jobInstance.jobName,
            jobExecution.id,
            jobExecution.jobParameters,
        )
    }

    override fun afterJob(jobExecution: JobExecution) {
        val duration = if (jobExecution.startTime != null && jobExecution.endTime != null) {
            Duration.between(jobExecution.startTime, jobExecution.endTime)
        } else {
            Duration.ZERO
        }

        log.info(
            "🏁 [{}] Job 완료. executionId={}, status={}, exitCode={}, 소요시간={}ms",
            jobExecution.jobInstance.jobName,
            jobExecution.id,
            jobExecution.status,
            jobExecution.exitStatus.exitCode,
            duration.toMillis(),
        )

        if (jobExecution.exitStatus.exitDescription.isNotBlank()) {
            log.warn(
                "⚠️ [{}] 종료 설명: {}",
                jobExecution.jobInstance.jobName,
                jobExecution.exitStatus.exitDescription,
            )
        }
    }
}