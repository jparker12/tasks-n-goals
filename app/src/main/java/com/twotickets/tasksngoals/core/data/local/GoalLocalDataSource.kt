package com.twotickets.tasksngoals.core.data.local

import kotlinx.coroutines.flow.Flow

interface GoalLocalDataSource {

    fun getGoals(): Flow<List<GoalWithTargets>>

    suspend fun upsertGoal(goalWithTargets: GoalWithTargets) // : Result<Etc> ??

    suspend fun deleteGoal(goalId: Long)

}