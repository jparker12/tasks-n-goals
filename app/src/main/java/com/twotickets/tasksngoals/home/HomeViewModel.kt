package com.twotickets.tasksngoals.home

import androidx.lifecycle.*
import com.twotickets.tasksngoals.core.domain.model.Task
import com.twotickets.tasksngoals.core.domain.repository.TaskRepository
import com.twotickets.tasksngoals.core.domain.repository.TaskRepository.CompleteTaskResult
import com.twotickets.tasksngoals.core.utils.OneShot
import com.twotickets.tasksngoals.core.utils.toViewFormat
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class HomeViewModel(
    private val taskRepository: TaskRepository
): ViewModel() {

    val tasks = taskRepository.getTasks().asLiveData()

    val snackbar = MutableLiveData<OneShot<String>>()

    val openTaskDetail = MutableLiveData<OneShot<Task>>()

    fun completeTask(task: Task) {
        viewModelScope.launch {
            val result = taskRepository.completeTask(task)

            val message = when(result) {
                is CompleteTaskResult.Success -> "Good job!"
                is CompleteTaskResult.SuccessWithRepeat -> {
                    val dateText = result.deadlineDate.toViewFormat()
                    val timeText = result.deadlineTime?.format(DateTimeFormatter.ofPattern("' at 'HH:mm")) ?: ""
                    "Good job! the next deadline is $dateText$timeText"
                }
                is CompleteTaskResult.Fail -> "Oops. Something went wrong."
            }

            snackbar.value = OneShot(message)
        }
    }

    fun onTaskClick(task: Task) {
        openTaskDetail.value = OneShot(task)
    }

    class Factory(
        private val taskRepository: TaskRepository
    ): ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                    HomeViewModel(taskRepository) as T
                }
                else -> throw IllegalArgumentException("Unsupported view model class, $modelClass")
            }
        }
    }
}