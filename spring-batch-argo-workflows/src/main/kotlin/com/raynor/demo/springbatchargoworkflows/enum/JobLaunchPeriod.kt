package com.raynor.demo.springbatchargoworkflows.enum

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class JobLaunchPeriod(
    private val truncateUnit: ChronoUnit,
    private val minuteInterval: Int,
    private val pattern: String,
) {
    DAILY(ChronoUnit.DAYS, 0, "yyyy-MM-dd"),
    HOURLY(ChronoUnit.HOURS, 0, "yyyy-MM-dd HH"),
    MINUTELY_30(ChronoUnit.MINUTES, 30, "yyyy-MM-dd HH:mm"),
    MINUTELY_15(ChronoUnit.MINUTES, 15, "yyyy-MM-dd HH:mm"),
    MINUTELY_5(ChronoUnit.MINUTES, 5, "yyyy-MM-dd HH:mm"),
    MINUTELY_1(ChronoUnit.MINUTES, 1, "yyyy-MM-dd HH:mm"),
    NONE(ChronoUnit.MICROS, 0, "yyyy-MM-dd HH:mm:ss.SSSSSS"),
    ;

    fun toParameterValue(): String {
        val now = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))

        val truncated = if (minuteInterval > 0) {
            val minute = now.minute - (now.minute % minuteInterval)
            now.truncatedTo(ChronoUnit.HOURS).withMinute(minute)
        } else {
            now.truncatedTo(truncateUnit)
        }
        return truncated.format(DateTimeFormatter.ofPattern(pattern))
    }
}