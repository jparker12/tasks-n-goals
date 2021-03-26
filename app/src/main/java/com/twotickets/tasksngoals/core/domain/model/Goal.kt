package com.twotickets.tasksngoals.core.domain.model

import com.twotickets.tasksngoals.core.data.local.GoalEntity
import com.twotickets.tasksngoals.core.data.local.GoalWithTargets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Goal(
    val id: Long,
    val name: String,
    val notes: String?,
    val deadlineDate: LocalDate,
    val completedDateTime: LocalDateTime?,
    val targets: List<GoalTarget> = emptyList()
)

fun GoalWithTargets.toDomain() = Goal(
    id = goal.id,
    name = goal.name,
    notes = goal.notes,
    deadlineDate = LocalDate.parse(goal.deadlineDate),
    completedDateTime = goal.completedDateTime?.let { LocalDateTime.parse(it) },
    targets = targets.map { it.toDomain() }
)

fun Goal.toLocal() = GoalWithTargets(
    goal = GoalEntity(
        id = id,
        name = name,
        notes = notes,
        deadlineDate = deadlineDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
        completedDateTime = completedDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    ),
    targets = targets.map {
        if (it.goalId != id) {
            throw IllegalArgumentException("Goal with ID=$id contains a GoalTarget whose parent " +
                    "is not this goal (targetId=${it.id}, goalId=${it.goalId}, description='${it.description}'")
        }
        it.toLocal()
    }
)
