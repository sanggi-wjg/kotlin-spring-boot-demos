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
    fun longRunningJob(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .listener(jobCompletionListener)
            .start(longRunningJobStep())
            .build()
    }

    @Bean
    fun longRunningJobStep(): Step {
        return StepBuilder(STEP_NAME, jobRepository).tasklet({ _, chunkContext ->
            val params = chunkContext.stepContext.jobParameters
            log.info("⏳ $STEP_NAME 실행 중. parameters: {}", params)

            // 약 8분(500초) 슬립
            repeat(500) { i ->
                Thread.sleep(1_000)
                if (i % 60 == 0) {
                    log.info("⏳ $STEP_NAME 실행 중. 경과: ${i}초")
                }
            }
            log.info("✅ $STEP_NAME 완료")

            RepeatStatus.FINISHED
        }, transactionManager).build()
    }
}