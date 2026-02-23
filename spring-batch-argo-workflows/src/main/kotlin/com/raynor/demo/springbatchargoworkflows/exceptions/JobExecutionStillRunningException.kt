package com.raynor.demo.springbatchargoworkflows.exceptions

import org.springframework.batch.core.BatchStatus

class JobExecutionStillRunningException(val status: BatchStatus) : RuntimeException(
    "Job execution is still running with status: $status"
)
