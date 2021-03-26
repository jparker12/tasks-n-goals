package com.twotickets.tasksngoals.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.twotickets.tasksngoals.R
import com.twotickets.tasksngoals.core.domain.model.RepeatInterval
import com.twotickets.tasksngoals.core.domain.model.Task
import com.twotickets.tasksngoals.core.utils.toViewFormat
import com.twotickets.tasksngoals.databinding.ItemTaskBinding
import java.time.format.DateTimeFormatter

class HomeAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCompleteClick: (Task) -> Unit
): ListAdapter<Task, HomeAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback:DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val task = getItem(adapterPosition)
                onTaskClick(task)
            }
            binding.cbComplete.setOnCheckedChangeListener { _, isChecked ->
                // Should never be false...
                if (isChecked) {
                    val task = getItem(adapterPosition)
                    onTaskCompleteClick(task)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        with(holder.binding) {
            tvTitle.text = task.name

            val dateText = task.deadlineDate.toViewFormat()
            val timeText = task.deadlineTime?.format(DateTimeFormatter.ofPattern("' at 'HH:mm")) ?: ""
            tvDeadline.text = dateText + timeText

            tvRepeat.text = if (task.repeatInterval == null) {
                tvRepeat.context.getString(R.string.does_not_repeat_after_completion)
            } else {
                val pluralRes = when(task.repeatInterval.timeUnit) {
                    RepeatInterval.TimeUnit.DAYS -> R.plurals.repeat_x_days_from_completion
                    RepeatInterval.TimeUnit.WEEKS -> R.plurals.repeat_x_weeks_from_completion
                    RepeatInterval.TimeUnit.MONTHS -> R.plurals.repeat_x_months_from_completion
                    RepeatInterval.TimeUnit.YEARS -> R.plurals.repeat_x_years_from_completion
                }
                val interval = task.repeatInterval.interval
                tvRepeat.context.resources.getQuantityString(pluralRes, interval, interval)
            }

            cbComplete.isChecked = false
        }
    }

}