package com.example.data.sync

data class FirestoreUserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val schemaVersion: Int = 1
)

data class FirestoreAcademicProfile(
    val hasProfile: Boolean = false,
    val departmentId: String = "",
    val departmentName: String = "",
    val programme: String = "",
    val programmeId: String = "",
    val regulation: String = "R25",
    val currentSemester: Int = 1,
    val activeRoadmapId: String = "",
    val activeRoadmapTitle: String = "",
    val currentTopicId: String = "",
    val currentTopicTitle: String = "",
    val selectedSubjects: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L
)

data class FirestoreLearningProgress(
    val key: String = "", // format: "roadmapId#nodeId"
    val roadmapId: String = "",
    val nodeId: String = "",
    val nodeTitle: String = "",
    val category: String = "",
    val status: String = "NOT_STARTED", // NOT_STARTED, IN_PROGRESS, COMPLETED, NEEDS_REVIEW
    val quizScore: Int? = null,
    val completedAt: Long? = null,
    val studiedInChat: Boolean = false,
    val sessionsCount: Int = 0,
    val studyTimeMinutes: Int = 0,
    val lastStudiedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L
)

data class FirestoreRoadmapSummaryProgress(
    val roadmapId: String = "",
    val title: String = "",
    val icon: String = "",
    val isCurrent: Boolean = false,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val percentage: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L
)

data class FirestoreQuizResult(
    val id: String = "",
    val roadmapId: String = "",
    val topicId: String = "",
    val topicTitle: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val weakConcepts: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isDailyQuiz: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class FirestoreUserPreferences(
    val streakDays: Int = 0,
    val longestStreak: Int = 0,
    val totalStudyMinutes: Int = 0,
    val lastActiveDate: String = "",
    val preferredAiModel: String = "gemini-3.5-flash",
    val updatedAt: Long = System.currentTimeMillis()
)

data class FirestoreSyncMetadata(
    val lastSyncTimestamp: Long = 0L,
    val clientAppVersion: String = "1.0.0",
    val syncEngineVersion: Int = 2,
    val totalSyncedTopics: Int = 0,
    val totalSyncedQuizzes: Int = 0,
    val lastSnapshotId: String? = null
)

data class FirestoreRecoverySnapshot(
    val snapshotId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reason: String = "Pre-synchronization safety snapshot",
    val academicProfile: FirestoreAcademicProfile? = null,
    val progressItemsCount: Int = 0,
    val quizResultsCount: Int = 0,
    val progressSnapshotJson: String = "" // compact JSON representation of previous progress
)

data class FirestoreSupportTicketPlaceholder(
    val ticketId: String = "",
    val uid: String = "",
    val subject: String = "",
    val description: String = "",
    val status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FirestoreStudyToolItem(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val subject: String = "",
    val topic: String = "",
    val syllabusContext: String = "",
    val contentJson: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
