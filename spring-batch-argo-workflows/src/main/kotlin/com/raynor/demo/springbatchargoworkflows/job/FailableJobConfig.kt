package com.raynor.demo.springbatchargoworkflows.job

import com.raynor.demo.springbatchargoworkflows.listener.JobCompletionListener
import org.slf4j.LoggerFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.support.ListItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class FailableJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val jobCompletionListener: JobCompletionListener,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val JOB_NAME = "failableJob"
        const val STEP_NAME = "${JOB_NAME}Step"
        const val CHUNK_SIZE = 2
        const val RETRY_LIMIT = 3L
    }

    @Bean
    fun failableJob(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .listener(jobCompletionListener)
            .start(failableStep())
            .build()
    }

    @Bean
    fun failableStep(): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<String, String>(CHUNK_SIZE)
            .transactionManager(transactionManager)
            .reader(failableItemReader())
            .processor(failableItemProcessor(null))
            .writer(failableItemWriter())
            .faultTolerant()
            .retryLimit(RETRY_LIMIT)
            .retry(RuntimeException::class.java)
            .build()
    }

    @Bean
    fun failableItemReader(): ListItemReader<String> {
        return ListItemReader(
            listOf("item-1", "item-2", "item-3", "item-4", "item-5")
        )
    }

    @Bean
    @StepScope
    fun failableItemProcessor(
        @Value("#{jobParameters['shouldFail']}") shouldFail: String?,
    ): ItemProcessor<String, String> {
        return ItemProcessor { item ->
            if (shouldFail.toBoolean()) {
                log.error("💥 FailableJob 프로세서 - shouldFail=true, 의도적 예외 발생. item: {}", item)
                throw RuntimeException("의도적 실패: $item")
            }
            log.info("⚙️ FailableJob 프로세서 - 아이템 처리 중: {}", item)
            item.uppercase()
        }
    }

    @Bean
    fun failableItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            items.forEach { item ->
                log.info("📝 FailableJob 라이터 - 아이템 기록: {}", item)
            }
        }
    }
}