package com.raynor.demo.springbatchargoworkflows.job

import com.raynor.demo.springbatchargoworkflows.listener.JobCompletionListener
import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class LongRunningJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val jobCompletionListener: JobCompletionListener,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val JOB_NAME = "longRunningJob"
        const val STEP_NAME = "${JOB_NAME}Step"
    }

    @Bean
    fun longJob(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(longJobStep())
            .listener(jobCompletionListener)
            .build()
    }

    @Bean
    fun longJobStep(): Step {
        return StepBuilder(STEP_NAME, jobRepository).tasklet({ _, chunkContext ->
            val params = chunkContext.stepContext.jobParameters
            log.info("⏳ $STEP_NAME 실행 중. parameters: {}", params)

            Thread.sleep(500_000)
            log.info("✅ $STEP_NAME 완료 (5초 sleep)")

            RepeatStatus.FINISHED
        }, transactionManager).build()
    }
}