package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TopicEntity::class, MessageEntity::class],
    version = 1,
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
