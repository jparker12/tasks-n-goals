package com.twotickets.tasksngoals.taskdetail

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.*
import com.twotickets.tasksngoals.R
import com.twotickets.tasksngoals.core.domain.model.RepeatInterval
import com.twotickets.tasksngoals.core.domain.model.Task
import com.twotickets.tasksngoals.core.domain.repository.TaskRepository
import com.twotickets.tasksngoals.core.utils.getDateTimeViewFormat
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@SuppressLint("NullSafeMutableLiveData")
class TaskViewModel(
    application: Application,
    private val taskId: Long?,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    private var editTask = Task(
        id = 0,
        name = "",
        notes = null,
        deadlineDate = LocalDate.now(),
        deadlineTime = null,
        completedDateTime = null,
        repeatInterval = RepeatInterval(1, RepeatInterval.TimeUnit.DAYS)
    )

    val task = MutableLiveData<Task>()

    val actionMode = MutableLiveData<ActionMode>()

    val onFinished = MutableLiveData<String?>()

    init {
        if (taskId == null) {
            task.value = editTask
            actionMode.value = ActionMode.CREATE
        } else {
            viewModelScope.launch {
                val repoTask = repository.getTask(taskId)
                if (repoTask != null) {
                    editTask = repoTask
                    task.value = editTask
                    actionMode.value = ActionMode.VIEW
                } else {
                    task.value = editTask
                    actionMode.value = ActionMode.CREATE
                }
            }
        }
    }

    fun beginEdit() {
        actionMode.value = ActionMode.EDIT
    }

    fun cancelEdit() {
        actionMode.value = ActionMode.VIEW
    }

    fun deleteTask() {
        val taskId = this.taskId ?: return
        viewModelScope.launch {
            repository.deleteTask(taskId)
            onFinished.value = "Task deleted"
        }
    }

    fun completeTask() {
        val task = this.task.value ?: return
        viewModelScope.launch {
            val result = repository.completeTask(task)

            val app = getApplication<Application>()
            val message = when(result) {
                is TaskRepository.CompleteTaskResult.Success -> app.getString(R.string.task_complete_message)
                is TaskRepository.CompleteTaskResult.SuccessWithRepeat -> {
                    val deadlineStr = getDateTimeViewFormat(result.deadlineDate, result.deadlineTime)
                    app.getString(R.string.task_complete_with_repeat_message, deadlineStr)
                }
                is TaskRepository.CompleteTaskResult.Fail -> app.getString(R.string.oops)
            }

            onFinished.value = message
        }
    }

    fun updateDraft(
        name: String,
        deadlineDate: LocalDate,
        deadlineTime: LocalTime,
        repeatInterval: RepeatInterval
    ) {
        actionMode.value?.takeUnless { it == ActionMode.VIEW } ?: return

        editTask = editTask.copy(
            name = name,
            deadlineDate = deadlineDate,
            deadlineTime = deadlineTime,
            repeatInterval = repeatInterval
        )
        task.value = editTask
    }

    fun saveChanges(
        name: String,
        deadlineDate: LocalDate,
        deadlineTime: LocalTime?,
        repeatInterval: RepeatInterval?
    ) {
        viewModelScope.launch {
            editTask = editTask.copy(
                name = name,
                deadlineDate = deadlineDate,
                deadlineTime = deadlineTime,
                repeatInterval = repeatInterval
            )

            repository.saveTask(editTask)

            onFinished.value = when (actionMode.value) {
                ActionMode.CREATE, ActionMode.EDIT -> "Task saved"
                else -> null
            }
        }
    }

    enum class ActionMode { CREATE, EDIT, VIEW }


    class Factory(
        private val application: Application,
        private val taskId: Long?,
        private val repository: TaskRepository
    ) : ViewModelProvider.AndroidViewModelFactory(application) {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(TaskViewModel::class.java) -> {
                    TaskViewModel(application, taskId, repository) as T
                }
                else -> throw IllegalArgumentException("Unsupported view model class, $modelClass")
            }
        }
    }

}