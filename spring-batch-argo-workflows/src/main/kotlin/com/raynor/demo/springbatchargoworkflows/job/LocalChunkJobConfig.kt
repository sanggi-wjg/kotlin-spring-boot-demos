package com.raynor.demo.springbatchargoworkflows.job

import com.raynor.demo.springbatchargoworkflows.config.BatchInfraConfig
import com.raynor.demo.springbatchargoworkflows.config.HeavyJob
import com.raynor.demo.springbatchargoworkflows.listener.JobCompletionListener
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.item.ChunkProcessor
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader
import org.springframework.batch.infrastructure.item.database.Order
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.infrastructure.item.database.support.MySqlPagingQueryProvider
import org.springframework.batch.integration.chunk.ChunkTaskExecutorItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import javax.sql.DataSource

@Configuration
class LocalChunkJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val jobCompletionListener: JobCompletionListener,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val JOB_NAME = "localChunkJob"
        const val STEP_NAME = "${JOB_NAME}Step"
        const val READER_NAME = "${JOB_NAME}Reader"
        const val WRITER_NAME = "${JOB_NAME}Writer"
        const val CHUNK_PROCESSOR_NAME = "${JOB_NAME}ChunkProcessor"
        const val CHUNK_SIZE = 10
    }

    @HeavyJob
    @Bean(JOB_NAME)
    fun multiThreadUserJob(
        @Qualifier(STEP_NAME) step: Step,
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .listener(jobCompletionListener)
            .start(step)
            .build()
    }

    @Bean(STEP_NAME)
    fun step(
        @Qualifier(READER_NAME) reader: JdbcPagingItemReader<Int>,
        @Qualifier(WRITER_NAME) writer: ChunkTaskExecutorItemWriter<Int>,
    ): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<Int, Int>(CHUNK_SIZE)
            .transactionManager(transactionManager)
            .reader(reader)
            .writer(writer)
            .build()
    }

    @Bean(READER_NAME)
    fun reader(
        dataSource: DataSource,
    ): JdbcPagingItemReader<Int> {
        val queryProvider = MySqlPagingQueryProvider().apply {
            this.setSelectClause("id")
            this.setFromClause("users")
            this.sortKeys = mapOf("id" to Order.ASCENDING)
        }

        return JdbcPagingItemReaderBuilder<Int>()
            .name(READER_NAME)
            .dataSource(dataSource)
            .queryProvider(queryProvider)
            .pageSize(CHUNK_SIZE)
            .rowMapper { rs, _ -> rs.getInt("id") }
            .saveState(false)
            .build()
    }

    @Bean(CHUNK_PROCESSOR_NAME)
    fun chunkProcessor(): ChunkProcessor<Int> {
        val transactionTemplate = TransactionTemplate(transactionManager)
        // https://docs.spring.io/spring-batch/reference/scalability.html#localChunking

        return ChunkProcessor { chunk, contribution ->
            transactionTemplate.executeWithoutResult { transactionStatus ->
                try {
                    logger.info("Writing {}", chunk.items.map { it })
                    Thread.sleep(5_000)

                    contribution.incrementWriteCount(chunk.size().toLong())
                    contribution.exitStatus = ExitStatus.COMPLETED
                } catch (e: Exception) {
                    transactionStatus.setRollbackOnly()
                    contribution.exitStatus = ExitStatus.FAILED.addExitDescription(e)
                }
            }
        }
    }

    @Bean(WRITER_NAME)
    fun writer(
        @Qualifier(CHUNK_PROCESSOR_NAME) chunkProcessor: ChunkProcessor<Int>,
        @Qualifier(BatchInfraConfig.LOCAL_CHUNK_EXECUTOR) taskExecutor: ThreadPoolTaskExecutor,
    ): ChunkTaskExecutorItemWriter<Int> {
        return ChunkTaskExecutorItemWriter(chunkProcessor, taskExecutor)
    }
}