package com.twotickets.tasksngoals.taskdetail

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.KeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.OverScroller
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.twotickets.tasksngoals.R
import com.twotickets.tasksngoals.core.data.local.DbTaskLocalDataSource
import com.twotickets.tasksngoals.core.data.local.MainDatabase
import com.twotickets.tasksngoals.core.domain.model.RepeatInterval
import com.twotickets.tasksngoals.core.domain.repository.TaskRepositoryImpl
import com.twotickets.tasksngoals.core.utils.toViewFormat
import com.twotickets.tasksngoals.databinding.ActivityTaskDetailBinding
import java.time.*
import java.time.format.DateTimeFormatter

class TaskDetailActivity : AppCompatActivity() {

    companion object {
        const val ARG_TASK_ID = "task_id"
        const val ARG_RESULT_MESSAGE = "result_message"
    }

    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var viewModel: TaskViewModel

    private lateinit var editTextBackground: Drawable
    private lateinit var editTextKeyListener: KeyListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTaskDetailBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val taskId = intent.getLongExtra(ARG_TASK_ID, -1L).takeUnless { it == -1L }
        val database = MainDatabase.getDatabase(this)
        val localDataSource = DbTaskLocalDataSource(database)
        val repository = TaskRepositoryImpl(localDataSource)
        viewModel = ViewModelProvider(
            this,
            TaskViewModel.Factory(application, taskId, repository)
        ).get(TaskViewModel::class.java)

        editTextBackground = binding.etTitle.background
        editTextKeyListener = binding.etTitle.keyListener

        with(binding) {
            switchAllDay.setOnCheckedChangeListener { _, isChecked ->
                tvTime.visibility = if (isChecked) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                if (tvTime.text.isNullOrBlank()) {
                    val now = LocalTime.now()
                    tvTime.text = now.toViewFormat()
                    tvTime.tag = now
                }
            }
        }

        with(binding) {
            tvDate.setOnClickListener {
                val curSelectedDate = tvDate.tag as? LocalDate ?: LocalDate.now()

                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setCalendarConstraints(
                        CalendarConstraints.Builder()
                            .setValidator(DateValidatorPointForward.now())
                            .build()
                    )
                    .setSelection(curSelectedDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .build()

                datePicker.addOnPositiveButtonClickListener {
                    val selectedDate = datePicker.selection?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.of("Z")).toLocalDate()
                    } ?: LocalDate.now()
                    tvDate.tag = selectedDate
                    tvDate.text = selectedDate.toViewFormat()
                    datePicker.clearOnPositiveButtonClickListeners()
                }

                datePicker.show(supportFragmentManager, "time_picker")
            }
        }

        with(binding) {
            tvTime.setOnClickListener {
                val localTime = tvTime.tag as? LocalTime ?: LocalTime.now()

                val timePicker = MaterialTimePicker.Builder()
                    .setHour(localTime.hour)
                    .setMinute(localTime.minute)
                    .build()

                timePicker.addOnPositiveButtonClickListener {
                    val selectedTime = LocalTime.of(timePicker.hour, timePicker.minute)
                    tvTime.tag = selectedTime
                    tvTime.text = selectedTime.toViewFormat()
                    timePicker.clearOnPositiveButtonClickListeners()
                }

                timePicker.show(supportFragmentManager, "time_picker")
            }
        }

        with(binding) {
            etRepeatNumber.addTextChangedListener { text ->
                val number = text?.toString()?.toIntOrNull() ?: 1
                if (number == 0) {
                    etRepeatNumber.setText("1")
                    return@addTextChangedListener
                }
                val curTimeUnitPluralId = etRepeatTimeUnit.tag as? Int ?: R.plurals.days
                etRepeatTimeUnit.setText(resources.getQuantityString(curTimeUnitPluralId, number))
            }
        }

        with(binding) {
            etRepeatTimeUnit.tag = R.plurals.days
            etRepeatTimeUnit.keyListener = null
            etRepeatTimeUnit.setOnClickListener {
                PopupMenu(this@TaskDetailActivity, etRepeatTimeUnit).apply {
                    val menuRes = if (etRepeatNumber.text?.toString()?.toIntOrNull() ?: 1 > 1) {
                        R.menu.task_repeat_time_unit_plural
                    } else {
                        R.menu.task_repeat_time_unit
                    }
                    menuInflater.inflate(menuRes, menu)
                    setOnMenuItemClickListener { menuItem ->
                        etRepeatTimeUnit.setText(menuItem.title)
                        etRepeatTimeUnit.tag = when (menuItem.itemId) {
                            R.id.menu_item_day -> R.plurals.days
                            R.id.menu_item_week -> R.plurals.weeks
                            R.id.menu_item_month -> R.plurals.months
                            R.id.menu_item_year -> R.plurals.years
                            else -> -1
                        }
                        if (menuItem.itemId == R.id.menu_item_does_not_repeat) {
                            tvRepeat.visibility = View.GONE
                            etRepeatNumber.visibility = View.GONE
                        } else if (tvRepeat.visibility != View.VISIBLE) {
                            tvRepeat.visibility = View.VISIBLE
                            etRepeatNumber.setText("1")
                            etRepeatNumber.visibility = View.VISIBLE
                        }
                        true
                    }
                    show()
                }
            }
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_edit -> {
                    viewModel.beginEdit()
                    true
                }
                R.id.menu_item_delete -> {
                    viewModel.deleteTask()
                    true
                }
                else -> false
            }
        }

        viewModel.task.observe(this) { task ->
            task ?: return@observe

            with(binding) {
                etTitle.setText(task.name)

                tvDate.tag = task.deadlineDate
                tvDate.text = task.deadlineDate.toViewFormat()

                if (task.deadlineTime == null) {
                    switchAllDay.isChecked = true
                    tvTime.visibility = View.GONE
                    tvTime.tag = null
                    tvTime.text = ""
                } else {
                    switchAllDay.isChecked = false
                    tvTime.tag = task.deadlineTime
                    tvTime.text = task.deadlineTime.toViewFormat()
                }

                if (task.repeatInterval == null) {
                    tvRepeatDetail.text = getString(R.string.does_not_repeat_after_completion)

                    tvRepeat.visibility = View.GONE
                    etRepeatNumber.visibility = View.GONE
                    etRepeatTimeUnit.setText(getString(R.string.does_not_repeat))
                    etRepeatTimeUnit.tag = -1
                } else {
                    val detailPluralRes: Int
                    val editPluralRes: Int
                    when (task.repeatInterval.timeUnit) {
                        RepeatInterval.TimeUnit.DAYS -> {
                            detailPluralRes = R.plurals.repeat_x_days_from_completion
                            editPluralRes = R.plurals.days
                        }
                        RepeatInterval.TimeUnit.WEEKS -> {
                            detailPluralRes = R.plurals.repeat_x_weeks_from_completion
                            editPluralRes = R.plurals.weeks
                        }
                        RepeatInterval.TimeUnit.MONTHS -> {
                            detailPluralRes = R.plurals.repeat_x_months_from_completion
                            editPluralRes = R.plurals.months
                        }
                        RepeatInterval.TimeUnit.YEARS -> {
                            detailPluralRes = R.plurals.repeat_x_years_from_completion
                            editPluralRes = R.plurals.years
                        }
                    }
                    val interval = task.repeatInterval.interval

                    tvRepeatDetail.text =
                        resources.getQuantityString(detailPluralRes, interval, interval)

                    tvRepeat.visibility = View.VISIBLE
                    etRepeatNumber.setText(interval.toString())
                    etRepeatTimeUnit.setText(resources.getQuantityString(editPluralRes, interval))
                    etRepeatTimeUnit.tag = editPluralRes
                }
            }
        }

        viewModel.actionMode.observe(this) { actionMode ->
            actionMode ?: return@observe

            with(binding) {
                if (actionMode == TaskViewModel.ActionMode.VIEW) {
                    etTitle.setBackgroundColor(Color.TRANSPARENT)
                    etTitle.isCursorVisible = false
                    etTitle.keyListener = null

                    tvDate.isClickable = false
                    tvTime.isClickable = false
                    switchAllDay.isClickable = false
                    layoutEditRepeat.visibility = View.GONE

                    tvRepeatDetail.visibility = View.VISIBLE
                } else {
                    etTitle.background = editTextBackground
                    etTitle.isCursorVisible = true
                    etTitle.keyListener = editTextKeyListener

                    tvDate.isClickable = true
                    tvTime.isClickable = true
                    switchAllDay.isClickable = true
                    layoutEditRepeat.visibility = View.VISIBLE

                    tvRepeatDetail.visibility = View.GONE
                }

                when (actionMode) {
                    TaskViewModel.ActionMode.CREATE -> {
                        bottomAppBar.setNavigationIcon(R.drawable.ic_arrow_back_24)
                        bottomAppBar.setNavigationOnClickListener { onBackPressed() }
                        bottomAppBar.menu.findItem(R.id.menu_item_edit)?.isVisible = false
                        bottomAppBar.menu.findItem(R.id.menu_item_delete)?.isVisible = false
                        fab.setImageResource(R.drawable.ic_outline_save_24)
                        fab.setOnClickListener {
                            saveChanges()
                        }
                    }
                    TaskViewModel.ActionMode.EDIT -> {
                        bottomAppBar.setNavigationIcon(R.drawable.ic_close_24)
                        bottomAppBar.setNavigationOnClickListener { viewModel.cancelEdit() }
                        bottomAppBar.menu.findItem(R.id.menu_item_edit)?.isVisible = false
                        bottomAppBar.menu.findItem(R.id.menu_item_delete)?.isVisible = false
                        fab.setImageResource(R.drawable.ic_outline_save_24)
                        fab.setOnClickListener {
                            saveChanges()
                        }
                    }
                    TaskViewModel.ActionMode.VIEW -> {
                        bottomAppBar.setNavigationIcon(R.drawable.ic_arrow_back_24)
                        bottomAppBar.setNavigationOnClickListener { onBackPressed() }
                        bottomAppBar.menu.findItem(R.id.menu_item_edit)?.isVisible = true
                        bottomAppBar.menu.findItem(R.id.menu_item_delete)?.isVisible = true
                        fab.setImageResource(R.drawable.ic_complete_24)
                        fab.setOnClickListener {
                            viewModel.completeTask()
                        }
                    }
                }
            }
        }

        viewModel.onFinished.observe(this) { resultMessage ->
            val resultIntent = Intent()
            resultMessage?.let { resultIntent.putExtra(ARG_RESULT_MESSAGE, it) }
            setResult(RESULT_OK, resultIntent)
            finish()
        }


        with(binding) {
            // scroll so the card is in the center of the activity
            nestedScrollView.viewTreeObserver.addOnPreDrawListener(preDrawListener)
            // disable scroll ripple effects
            nestedScrollView.overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        with(binding) {
            val scrollTo = (nestedScrollView.height/2) - (mtrlCardTask.height/2) - (resources.displayMetrics.density*28f).toInt()
            nestedScrollView.scrollTo(0, scrollTo)
        }
        removePreDrawListener()
        true
    }

    fun removePreDrawListener() {
        binding.nestedScrollView.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
    }

    private fun saveChanges() {
        with(binding) {
            val title = etTitle.text?.toString()
            if (title.isNullOrBlank()) {
                Snackbar.make(binding.root, getString(R.string.error_empty_task_name), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.fab)
                    .show()
                return
            }

            val deadlineDate = tvDate.tag as? LocalDate ?: LocalDate.now()

            val deadlineTime = if (switchAllDay.isChecked) {
                null
            } else {
                tvTime.tag as? LocalTime ?: LocalTime.now()
            }
            if (deadlineTime != null && deadlineDate.atTime(deadlineTime.hour, deadlineTime.minute) <= LocalDateTime.now()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_deadline_in_past),
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(binding.fab)
                    .show()
                return
            }

            val repeatInterval = if (etRepeatNumber.visibility == View.GONE) {
                null
            } else {
                val interval = etRepeatNumber.text?.toString()?.toIntOrNull() ?: 1
                val pluralRes = etRepeatTimeUnit.tag as? Int
                val timeUnit = pluralRes?.let {
                    when (it) {
                        R.plurals.days -> RepeatInterval.TimeUnit.DAYS
                        R.plurals.weeks -> RepeatInterval.TimeUnit.WEEKS
                        R.plurals.months -> RepeatInterval.TimeUnit.MONTHS
                        R.plurals.years -> RepeatInterval.TimeUnit.YEARS
                        else -> null
                    }
                }
                timeUnit?.let {
                    RepeatInterval(interval, timeUnit)
                }
            }

            viewModel.saveChanges(title, deadlineDate, deadlineTime, repeatInterval)
        }
    }
}