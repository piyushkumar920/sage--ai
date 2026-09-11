package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val mode: String = "NORMAL", // NORMAL, LEARNING, SOCRATIC
    val goal: String = "",
    val currentPhase: String = "Ready",
    val roadmapJson: String = "",
    val quizScore: Int = 0,
    val totalQuizzes: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
