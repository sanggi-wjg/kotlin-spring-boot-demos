package com.raynor.demo.springbatchargoworkflows.exceptions

class JobNotFoundException(val jobName: String) : RuntimeException("Job not found: $jobName")
