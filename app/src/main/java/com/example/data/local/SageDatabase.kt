package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TopicEntity::class,
        MessageEntity::class,
        RoadmapProgressEntity::class,
        TopicProgressEntity::class,
        QuizResultEntity::class,
        DailyQuizRecordEntity::class,
        WeakConceptEntity::class,
        com.example.data.studytools.SavedStudyToolEntity::class,
        FocusSessionEntity::class,
        com.example.data.mission.DailyMissionEntity::class,
        com.example.data.profile.AcademicProfileEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class SageDatabase : RoomDatabase() {
    abstract fun sageDao(): SageDao

    companion object {
        @Volatile
        private var INSTANCE: SageDatabase? = null

        fun getInstance(context: Context): SageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SageDatabase::class.java,
                    "sage_ai_learning.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
