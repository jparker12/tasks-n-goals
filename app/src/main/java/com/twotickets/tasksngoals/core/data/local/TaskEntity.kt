package com.twotickets.tasksngoals.core.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val notes: String? = null,

    @ColumnInfo(name = "deadline_date")
    val deadlineDate: String,

    @ColumnInfo(name = "deadline_time")
    val deadlineTime: String? = null,

    @ColumnInfo(name = "completed_datetime")
    val completedDateTime: String? = null,

    @ColumnInfo(name = "repeat_interval")
    val repeatInterval: Int? = null,

    @ColumnInfo(name = "repeat_interval_time_unit")
    val repeatIntervalTimeUnit: String? = null
)
