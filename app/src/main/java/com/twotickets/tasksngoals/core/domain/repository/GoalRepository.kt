package com.twotickets.tasksngoals.core.domain.repository

import com.twotickets.tasksngoals.core.data.local.GoalLocalDataSource
import com.twotickets.tasksngoals.core.domain.model.Goal
import com.twotickets.tasksngoals.core.domain.model.toDomain
import com.twotickets.tasksngoals.core.domain.model.toLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface GoalRepository {

    fun getGoals(): Flow<List<Goal>>

    suspend fun upsertGoal(goal: Goal)

    suspend fun deleteGoal(goal: Goal)
}

class GoalRepositoryImpl(
    private val localDataSource: GoalLocalDataSource
): GoalRepository {

    override fun getGoals(): Flow<List<Goal>> {
        return localDataSource.getGoals()
            .map { goalsWithTargets -> goalsWithTargets.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun upsertGoal(goal: Goal) {
        val localGoal = withContext(Dispatchers.IO) { goal.toLocal() }
        localDataSource.upsertGoal(localGoal)
    }

    override suspend fun deleteGoal(goal: Goal) {
        localDataSource.deleteGoal(goal.id)
    }
}