package com.raynor.demo.springbatchargoworkflows.config

import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.launch.JobOperator
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.findAnnotationOnBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.stereotype.Component

@Component
class JobOperatorResolver(
    beanFactory: ListableBeanFactory,
    @Qualifier(BatchInfraConfig.JOB_OPERATOR_HEAVY) private val heavyJobOperator: JobOperator,
    @Qualifier(BatchInfraConfig.JOB_OPERATOR_LIGHT) private val lightJobOperator: JobOperator,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val heavyJobNames: Set<String> = buildSet {
        beanFactory.getBeansOfType<Job>().forEach { (beanName, job) ->
            val annotations = beanFactory.findAnnotationOnBean<HeavyJob>(beanName)
            if (annotations != null) {
                add(job.name)
            }
        }
        log.info("Heavy jobs resolved: {}", this)
    }

    fun resolve(jobName: String): JobOperator {
        return if (jobName in heavyJobNames) heavyJobOperator else lightJobOperator
    }

    fun isHeavy(jobName: String): Boolean = jobName in heavyJobNames
}