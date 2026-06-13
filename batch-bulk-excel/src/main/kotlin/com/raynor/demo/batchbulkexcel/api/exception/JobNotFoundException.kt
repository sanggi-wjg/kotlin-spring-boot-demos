package com.raynor.demo.batchbulkexcel.api.exception

class JobNotFoundException(
    jobId: String,
) : RuntimeException("job 을 찾을 수 없습니다: $jobId")
