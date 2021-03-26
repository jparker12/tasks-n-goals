package com.twotickets.tasksngoals.core.domain.model

import com.twotickets.tasksngoals.core.data.local.TaskEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Task(
    val id: Long,
    val name: String,
    val notes: String?,
    val deadlineDate: LocalDate,
    val deadlineTime: LocalTime?,
    val completedDateTime: LocalDateTime?,
    val repeatInterval: RepeatInterval?
)

data class RepeatInterval(
    val interval: Int,
    val timeUnit: TimeUnit
) {
    enum class TimeUnit {
        DAYS, WEEKS, MONTHS, YEARS
    }
}

fun Task.toLocal(): TaskEntity = TaskEntity(
    id = id,
    name = name,
    notes = notes,
    deadlineDate = deadlineDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
    deadlineTime = deadlineTime?.format(DateTimeFormatter.ISO_LOCAL_TIME),
    completedDateTime = completedDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    repeatInterval = repeatInterval?.interval,
    repeatIntervalTimeUnit = repeatInterval?.timeUnit?.name
)

fun TaskEntity.toDomain() = Task(
    id = id,
    name = name,
    notes = notes,
    deadlineDate = LocalDate.parse(deadlineDate, DateTimeFormatter.ISO_LOCAL_DATE),
    deadlineTime = deadlineTime?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) },
    completedDateTime = completedDateTime?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) },
    repeatInterval = if (repeatInterval != null && repeatIntervalTimeUnit != null) {
        try {
            val timeUnit = RepeatInterval.TimeUnit.valueOf(repeatIntervalTimeUnit)
            RepeatInterval(repeatInterval, timeUnit)
        } catch (e: IllegalArgumentException) {
            null
        }
    } else {
        null
    }
)
