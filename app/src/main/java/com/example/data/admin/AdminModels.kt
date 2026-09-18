package com.example.data.admin

import com.example.data.sync.FirestoreAcademicProfile
import com.example.data.sync.FirestoreLearningProgress
import com.example.data.sync.FirestoreQuizResult
import com.example.data.sync.FirestoreRecoverySnapshot
import com.example.data.sync.FirestoreSyncMetadata
import com.example.data.sync.FirestoreUserProfile

/**
 * Support Ticket representation for Sage AI Learning.
 * Stored under supportTickets/{ticketId}
 */
data class AdminSupportTicket(
    val ticketId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val category: String = "Other",
    val subject: String = "",
    val description: String = "",
    val diagnostics: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    val adminNote: String = "",
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null
)

/**
 * Admin Audit Log entry.
 * Stored under adminAuditLogs/{logId}
 */
data class AdminAuditLogEntry(
    val logId: String = "",
    val adminUid: String = "",
    val adminName: String = "Piyush Kumar",
    val action: String = "",
    val targetUserId: String = "",
    val targetTicketId: String = "",
    val targetSnapshotId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Comprehensive user profile item displayed in User Management.
 */
data class AdminUserProfileItem(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val createdAt: Long = 0L,
    val lastSyncTime: Long = 0L,
    val lastActiveTime: Long = 0L,
    val programme: String = "B. Tech CSE (AI & ML)",
    val regulation: String = "R25",
    val semester: Int = 1,
    val currentCourse: String = "CS101",
    val currentTopic: String = "",
    val progressPercentage: Int = 0,
    val completedTopicsCount: Int = 0,
    val inProgressTopicsCount: Int = 0,
    val totalTopicsCount: Int = 0,
    val quizzesCount: Int = 0,
    val averageScore: Int = 0,
    val syncStatus: String = "Active",
    val lastSyncError: String? = null
)

/**
 * Detailed user state for User Details inspection.
 */
data class AdminUserDetails(
    val userProfile: FirestoreUserProfile,
    val academicProfile: FirestoreAcademicProfile?,
    val progressList: List<FirestoreLearningProgress>,
    val quizResults: List<FirestoreQuizResult>,
    val syncMetadata: FirestoreSyncMetadata?,
    val snapshots: List<FirestoreRecoverySnapshot>
)

/**
 * Complete runtime system health report.
 */
data class SystemHealthReport(
    val timestamp: Long = System.currentTimeMillis(),
    val networkConnected: Boolean = false,
    val networkDetails: String = "",
    val firebaseAuthConnected: Boolean = false,
    val currentAuthUid: String = "",
    val currentAuthEmail: String = "",
    val firestoreConnected: Boolean = false,
    val firestoreDetails: String = "",
    val renderBackendConnected: Boolean = false,
    val renderBackendUrl: String = "",
    val renderBackendStatus: String = "",
    val renderBackendLatencyMs: Long = -1L,
    val geminiConnected: Boolean = false,
    val geminiModel: String = "",
    val roomDatabaseConnected: Boolean = false,
    val roomStats: String = "",
    val cloudSyncState: String = "",
    val cloudSyncTimestamp: Long = 0L,
    val lastKnownError: String? = null
)
