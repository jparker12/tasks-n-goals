package com.twotickets.tasksngoals.core.domain.model

import com.twotickets.tasksngoals.core.data.local.GoalTargetEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class GoalTarget(
    val id: Long = 0,
    val goalId: Long,
    val description: String,
    val progress: Progress?,
    val completedDateTime: LocalDateTime?
) {
    data class Progress(
        val start: Int,
        val target: Int,
        val units: String?
    )
}

fun GoalTargetEntity.toDomain(): GoalTarget {
    val progress = if (progressStart != null && progressTarget != null) {
        GoalTarget.Progress(progressStart, progressTarget, progressUnits)
    } else {
        null
    }

    val domainCompletedDateTime = LocalDateTime.parse(completedDateTime)

    return GoalTarget(
        id = id,
        goalId = goalId,
        description = description,
        progress = progress,
        completedDateTime = domainCompletedDateTime
    )
}

fun GoalTarget.toLocal() = GoalTargetEntity(
    id = id,
    goalId = goalId,
    description = description,
    progressStart = progress?.start,
    progressTarget = progress?.target,
    progressUnits = progress?.units,
    completedDateTime = completedDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
)
