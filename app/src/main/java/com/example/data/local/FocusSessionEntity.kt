package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local Room Entity representing a completed or ended Focus Mode learning session.
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long = System.currentTimeMillis(),
    val plannedDurationMinutes: Int = 25,
    val actualFocusedSeconds: Int = 0,
    val department: String = "",
    val programme: String = "B. Tech",
    val regulation: String = "R25",
    val semester: Int? = null,
    val courseCode: String = "",
    val courseName: String = "",
    val module: String = "",
    val topic: String = "",
    val goal: String = "",
    val learnCompleted: Boolean = false,
    val practiceCompleted: Boolean = false,
    val reviewCompleted: Boolean = false,
    val completedNormally: Boolean = true
)
