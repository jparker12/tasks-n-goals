package com.twotickets.tasksngoals.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, GoalEntity::class, GoalTargetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MainDatabase: RoomDatabase() {

    abstract fun taskDao(): TaskDao

    abstract fun goalDao(): GoalDao

    companion object {

        @Volatile
        private var INSTANCE: MainDatabase? = null

        fun getDatabase(context: Context): MainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDatabase::class.java,
                    "tasksngoals_db"
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }

}