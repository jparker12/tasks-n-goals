package com.twotickets.tasksngoals.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task WHERE completed_datetime IS NULL ORDER BY deadline_date ASC, deadline_time ASC")
    fun getTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id=:taskId")
    suspend fun getTask(taskId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM task WHERE id=:taskId")
    suspend fun deleteTask(taskId: Long)

}