package com.raynor.demo.batchbulkexcel.message

data class JobCompletedEvent(
    val jobId: String,
    val status: String,
    val requesterId: Long?,
    val summary: Summary,
) {
    data class Summary(
        val total: Int,
        val success: Int,
        val failed: Int,
    )
}
