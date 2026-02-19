package com.raynor.demo.springbatchargoworkflows.exceptions

class JobExecutionNotFoundException(val executionId: Long) : RuntimeException("Job execution not found: $executionId")
