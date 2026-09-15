package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roadmap_progress")
data class RoadmapProgressEntity(
    @PrimaryKey
    val roadmapId: String,
    val title: String,
    val icon: String,
    val isCurrent: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "topic_progress")
data class TopicProgressEntity(
    @PrimaryKey
    val key: String, // format: "roadmapId#nodeId"
    val roadmapId: String,
    val nodeId: String,
    val nodeTitle: String,
    val category: String,
    val status: String = "NOT_STARTED", // NOT_STARTED, IN_PROGRESS, COMPLETED, NEEDS_REVIEW
    val quizScore: Int? = null,
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val studiedInChat: Boolean = false,
    val sessionsCount: Int = 0,
    val studyTimeMinutes: Int = 0,
    val lastStudiedAt: Long? = null
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roadmapId: String,
    val topicId: String,
    val topicTitle: String,
    val score: Int, // percentage e.g. 75
    val totalQuestions: Int,
    val weakConcepts: String = "", // comma separated
    val timestamp: Long = System.currentTimeMillis(),
    val isDailyQuiz: Boolean = false
)

@Entity(tableName = "daily_quiz_records")
data class DailyQuizRecordEntity(
    @PrimaryKey
    val dateStr: String, // e.g. "2026-09-13"
    val roadmapId: String,
    val topicId: String,
    val topicTitle: String,
    val question: String,
    val optionsJson: String, // JSON array string
    val correctIndex: Int,
    val explanation: String,
    val userAnswerIndex: Int? = null,
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false,
    val isMissed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weak_concepts")
data class WeakConceptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val concept: String,
    val roadmapId: String,
    val topicId: String,
    val mistakeCount: Int = 1,
    val lastTestedAt: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)
