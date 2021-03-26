package com.twotickets.tasksngoals.core.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String, // unique?

    val notes: String? = null,

    @ColumnInfo(name = "deadline_date")
    val deadlineDate: String,

    @ColumnInfo(name = "completed_datetime")
    val completedDateTime: String? = null,
)