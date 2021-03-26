package com.twotickets.tasksngoals.core.domain.repository

import com.twotickets.tasksngoals.core.data.local.TaskLocalDataSource
import com.twotickets.tasksngoals.core.domain.model.RepeatInterval
import com.twotickets.tasksngoals.core.domain.model.Task
import com.twotickets.tasksngoals.core.domain.model.toDomain
import com.twotickets.tasksngoals.core.domain.model.toLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

interface TaskRepository {

    fun getTasks(): Flow<List<Task>>

    suspend fun getTask(taskId: Long): Task?

    suspend fun saveTask(task: Task)

    suspend fun completeTask(task: Task): CompleteTaskResult

    suspend fun deleteTask(taskId: Long)

    sealed class CompleteTaskResult {
        object Success : CompleteTaskResult()
        data class SuccessWithRepeat(val deadlineDate: LocalDate, val deadlineTime: LocalTime?)
            : CompleteTaskResult()
        object Fail : CompleteTaskResult()
    }

}

class TaskRepositoryImpl(
    private val localDataSource: TaskLocalDataSource
) : TaskRepository {

    override fun getTasks(): Flow<List<Task>> {
        return localDataSource.getTasks()
            .map { taskList -> taskList.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getTask(taskId: Long): Task? {
        val taskEntity = localDataSource.getTask(taskId)
        return withContext(Dispatchers.IO) { taskEntity?.toDomain() }
    }

    override suspend fun saveTask(task: Task) {
        val taskEntity = withContext(Dispatchers.IO) { task.toLocal() }
        localDataSource.upsertTask(taskEntity)
    }

    override suspend fun completeTask(task: Task): TaskRepository.CompleteTaskResult {
        task.takeIf { it.completedDateTime == null }
            ?: return TaskRepository.CompleteTaskResult.Fail

        return withContext(Dispatchers.IO) {
            val completedTask = task.copy(completedDateTime = LocalDateTime.now())
            localDataSource.upsertTask(completedTask.toLocal())

            if (completedTask.repeatInterval != null) {
                val chronoUnit = when (completedTask.repeatInterval.timeUnit) {
                    RepeatInterval.TimeUnit.DAYS -> ChronoUnit.DAYS
                    RepeatInterval.TimeUnit.WEEKS -> ChronoUnit.WEEKS
                    RepeatInterval.TimeUnit.MONTHS -> ChronoUnit.MONTHS
                    RepeatInterval.TimeUnit.YEARS -> ChronoUnit.YEARS
                }
                val deadlineDateTime = completedTask.completedDateTime!!.plus(
                    completedTask.repeatInterval.interval.toLong(),
                    chronoUnit
                )
                val deadlineDate = deadlineDateTime.toLocalDate()
                val deadlineTime =
                    completedTask.deadlineTime?.let { deadlineDateTime.toLocalTime() }
                val repeatTask = completedTask.copy(
                    id = 0,
                    deadlineDate = deadlineDate,
                    deadlineTime = deadlineTime,
                    completedDateTime = null
                )
                localDataSource.upsertTask(repeatTask.toLocal())
                TaskRepository.CompleteTaskResult.SuccessWithRepeat(deadlineDate, deadlineTime)
            } else {
                TaskRepository.CompleteTaskResult.Success
            }
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        localDataSource.deleteTask(taskId)
    }
}