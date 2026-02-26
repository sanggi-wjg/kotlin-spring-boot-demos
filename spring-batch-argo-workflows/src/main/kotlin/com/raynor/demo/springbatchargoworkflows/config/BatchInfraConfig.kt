package com.raynor.demo.springbatchargoworkflows.config

import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.configuration.support.MapJobRegistry
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class BatchInfraConfig {

    @Bean
    fun jobRegistry(): JobRegistry {
        return MapJobRegistry()
    }

    companion object {
        const val JOB_EXECUTOR_HEAVY = "heavyTaskExecutor"
        const val JOB_EXECUTOR_LIGHT = "lightTaskExecutor"
        const val LOCAL_CHUNK_EXECUTOR = "localChunkExecutor"

        const val JOB_OPERATOR_HEAVY = "heavyJobOperator"
        const val JOB_OPERATOR_LIGHT = "lightJobOperator"
    }

    @Bean(JOB_EXECUTOR_HEAVY)
    fun heavyTaskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 3
            maxPoolSize = 5
            queueCapacity = 10
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(300)
            setThreadNamePrefix("heavy-")
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        }
    }

    @Bean(JOB_EXECUTOR_LIGHT)
    fun lightTaskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 5
            maxPoolSize = 10
            queueCapacity = 20
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(300)
            setThreadNamePrefix("light-")
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        }
    }

    @Bean(LOCAL_CHUNK_EXECUTOR)
    fun localChunkTaskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 5
            maxPoolSize = 10
            queueCapacity = 20
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(300)
            setThreadNamePrefix("async-")
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        }
    }

    @Bean(JOB_OPERATOR_HEAVY)
    fun heavyJobOperator(
        jobRepository: JobRepository,
        jobRegistry: JobRegistry,
        @Qualifier(JOB_EXECUTOR_HEAVY) taskExecutor: ThreadPoolTaskExecutor,
    ): JobOperator {
        return TaskExecutorJobOperator().apply {
            setJobRepository(jobRepository)
            setJobRegistry(jobRegistry)
            setTaskExecutor(taskExecutor)
        }
    }

    @Bean(JOB_OPERATOR_LIGHT)
    fun lightJobOperator(
        jobRepository: JobRepository,
        jobRegistry: JobRegistry,
        @Qualifier(JOB_EXECUTOR_LIGHT) taskExecutor: ThreadPoolTaskExecutor,
    ): JobOperator {
        return TaskExecutorJobOperator().apply {
            setJobRepository(jobRepository)
            setJobRegistry(jobRegistry)
            setTaskExecutor(taskExecutor)
        }
    }
}
