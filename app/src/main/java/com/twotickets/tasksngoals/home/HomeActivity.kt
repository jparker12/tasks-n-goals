package com.twotickets.tasksngoals.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.math.MathUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.twotickets.tasksngoals.R
import com.twotickets.tasksngoals.core.data.local.DbTaskLocalDataSource
import com.twotickets.tasksngoals.core.data.local.MainDatabase
import com.twotickets.tasksngoals.core.domain.repository.TaskRepositoryImpl
import com.twotickets.tasksngoals.databinding.ActivityHomeBinding
import com.twotickets.tasksngoals.taskdetail.TaskDetailActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var navDrawerBehavior: BottomSheetBehavior<NavigationView>

    private lateinit var viewModel: HomeViewModel

    private val startTaskDetailForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringExtra(TaskDetailActivity.ARG_RESULT_MESSAGE)?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.fab)
                .show()
        }
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = MainDatabase.getDatabase(this)
        val taskLocalDataSource = DbTaskLocalDataSource(database)
        val taskRepository = TaskRepositoryImpl(taskLocalDataSource)
        viewModel = ViewModelProvider(this, HomeViewModel.Factory(taskRepository)).get(HomeViewModel::class.java)

        binding = ActivityHomeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        navDrawerBehavior = BottomSheetBehavior.from(binding.navDrawer)
        navDrawerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        navDrawerBehavior.skipCollapsed = true

        // WARNING: This disables the animation that transforms the sheet to square corners in the expanded state.
        // The android designers were "strongly opinionated" that expanded sheets should have square corners... cheers.
        // It is marked as VisibleForTests and RestrictedApi
        navDrawerBehavior.disableShapeAnimations()

        binding.bottomAppBar.setNavigationOnClickListener {
            navDrawerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.navDrawer.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            navDrawerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            true
        }

        binding.scrim.setOnClickListener {
            navDrawerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        navDrawerBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                binding.scrim.visibility = when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> View.GONE
                    else -> View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val baseColor = Color.BLACK
                // 60% opacity
                val baseAlpha = ResourcesCompat.getFloat(resources, R.dimen.material_emphasis_medium)
                // Map slideOffset from [-1.0, 1.0] to [0.0, 1.0]
                val offset = ((slideOffset - (-1f)) / (1f - (-1f)) * (1f - 0f) + 0f)
                val alpha = (MathUtils.lerp(0f, 255f, offset * baseAlpha).toInt()).coerceAtLeast(0)
                val color = Color.argb(alpha, baseColor.red, baseColor.green, baseColor.blue)
                binding.scrim.setBackgroundColor(color)
            }
        })

        binding.fab.setOnClickListener {
            val intent = Intent(this, TaskDetailActivity::class.java)
            startTaskDetailForResult.launch(intent)
        }

        val homeAdapter = HomeAdapter(
            onTaskClick = { task ->
                viewModel.onTaskClick(task)
            },
            onTaskCompleteClick = { task ->
                viewModel.completeTask(task)
            }
        )
        with(binding) {
            recyclerView.adapter = homeAdapter
        }

        viewModel.openTaskDetail.observe(this) { oneShot ->
            val task = oneShot?.contentIfNotHandled ?: return@observe

            val intent = Intent(this, TaskDetailActivity::class.java).apply {
                putExtra(TaskDetailActivity.ARG_TASK_ID, task.id)
            }
            startTaskDetailForResult.launch(intent)
        }

        viewModel.snackbar.observe(this) { oneshot ->
            val message = oneshot?.contentIfNotHandled ?: return@observe

            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.fab)
                .show()
        }

        viewModel.tasks.observe(this) { taskList ->
            homeAdapter.submitList(taskList)
        }
    }
}