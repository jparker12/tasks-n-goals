package com.twotickets.tasksngoals.core.data.local

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class DbGoalLocalDataSource(
    private val database: MainDatabase
): GoalLocalDataSource {

    private val goalDao = database.goalDao()

    override fun getGoals(): Flow<List<GoalWithTargets>> {
        return goalDao.getGoals()
    }

    override suspend fun upsertGoal(goalWithTargets: GoalWithTargets) {
        database.withTransaction {
            if (goalDao.insertGoal(goalWithTargets.goal) < 0) {
                goalDao.updateGoal(goalWithTargets.goal)
            }
            goalWithTargets.targets.forEach {
                if (goalDao.insertTarget(it) < 0) {
                    goalDao.updateTarget(it)
                }
            }
        }
    }

    override suspend fun deleteGoal(goalId: Long) {
        database.withTransaction {
            goalDao.deleteGoal(goalId)
            goalDao.deleteTargets(goalId)
        }
    }
}