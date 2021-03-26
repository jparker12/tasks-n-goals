package com.twotickets.tasksngoals.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Transaction
    @Query("SELECT * FROM goal")
    fun getGoals(): Flow<List<GoalWithTargets>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goal WHERE id=:goalId")
    suspend fun deleteGoal(goalId: Long)

    @Query("DELETE FROM goal_target WHERE goal_id=:goalId")
    suspend fun deleteTargets(goalId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTarget(target: GoalTargetEntity): Long

    @Update
    suspend fun updateTarget(target: GoalTargetEntity)

    @Query("DELETE FROM goal_target WHERE id=:targetId")
    suspend fun deleteTarget(targetId: Long)

}