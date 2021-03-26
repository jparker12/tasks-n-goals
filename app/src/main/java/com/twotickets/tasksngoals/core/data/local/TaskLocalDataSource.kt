package com.twotickets.tasksngoals.core.data.local

import android.util.Log
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface TaskLocalDataSource {

    fun getTasks(): Flow<List<TaskEntity>>

    suspend fun getTask(taskId:Long): TaskEntity?

    suspend fun upsertTask(task: TaskEntity)

    suspend fun deleteTask(taskId: Long)

}

class DbTaskLocalDataSource(
    private val database: MainDatabase
): TaskLocalDataSource {

    private val taskDao = database.taskDao()

    override fun getTasks(): Flow<List<TaskEntity>> {
        return taskDao.getTasks()
            .distinctUntilChanged()
    }

    override suspend fun getTask(taskId: Long): TaskEntity? {
        return taskDao.getTask(taskId)
    }

    override suspend fun upsertTask(task: TaskEntity) {
        database.withTransaction {
            if (taskDao.insertTask(task) < 0) {
                taskDao.updateTask(task)
            }
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
    }
}