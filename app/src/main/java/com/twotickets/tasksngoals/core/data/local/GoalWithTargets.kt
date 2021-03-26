package com.twotickets.tasksngoals.core.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class GoalWithTargets(
    @Embedded val goal: GoalEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "goal_id"
    )
    val targets: List<GoalTargetEntity>
)