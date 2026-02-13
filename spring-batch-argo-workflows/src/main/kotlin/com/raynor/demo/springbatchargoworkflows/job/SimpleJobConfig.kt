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
class SimpleJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val jobCompletionListener: JobCompletionListener,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val JOB_NAME = "simpleJob"
        const val SIMPLE_STEP_NAME = "${JOB_NAME}SimpleStep"
        const val ANOTHER_STEP_NAME = "${JOB_NAME}AnotherStep"
    }

    @Bean
    fun sampleJob(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .listener(jobCompletionListener)
            .start(simpleStep())
            .next(anotherStep())
            .build()
    }

    @Bean
    fun simpleStep(): Step {
        return StepBuilder(SIMPLE_STEP_NAME, jobRepository).tasklet({ _, chunkContext ->
            val params = chunkContext.stepContext.jobParameters
            log.info("⏳ $SIMPLE_STEP_NAME 실행 중. parameters: {}", params)

            Thread.sleep(5_000)
            log.info("✅ $SIMPLE_STEP_NAME 완료 (5초 sleep)")

            RepeatStatus.FINISHED
        }, transactionManager).build()
    }

    @Bean
    fun anotherStep(): Step {
        return StepBuilder(ANOTHER_STEP_NAME, jobRepository).tasklet({ _, chunkContext ->
            val jobName = chunkContext.stepContext.jobName
            val params = chunkContext.stepContext.jobParameters

            log.info("⏳ $ANOTHER_STEP_NAME 실행 중 - 후처리 대상 job: {}", jobName)
            Thread.sleep(1_000)
            log.info("✅ $ANOTHER_STEP_NAME 완료 - 처리된 파라미터 수: {}", params.size)

            RepeatStatus.FINISHED
        }, transactionManager).build()
    }
}
