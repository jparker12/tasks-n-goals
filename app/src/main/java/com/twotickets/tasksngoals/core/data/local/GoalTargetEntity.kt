package com.twotickets.tasksngoals.core.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_target")
data class GoalTargetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "goal_id")
    val goalId: Long,

    val description: String,

    @ColumnInfo(name = "progress_start")
    val progressStart: Int?,

    @ColumnInfo(name = "progress_target")
    val progressTarget: Int?,

    @ColumnInfo(name = "progress_units")
    val progressUnits: String?,

    @ColumnInfo(name = "completed_date_time")
    val completedDateTime: String?
)
