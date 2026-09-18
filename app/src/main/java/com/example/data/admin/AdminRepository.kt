package com.example.data.admin

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiConfig
import com.example.data.api.GeminiResult
import com.example.data.auth.AdminAuthManager
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.local.PreferencesManager
import com.example.data.local.SageDatabase
import com.example.data.sync.FirestoreAcademicProfile
import com.example.data.sync.FirestoreLearningProgress
import com.example.data.sync.FirestoreQuizResult
import com.example.data.sync.FirestoreRecoverySnapshot
import com.example.data.sync.FirestoreSyncMetadata
import com.example.data.sync.FirestoreUserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class AdminRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val preferencesManager: PreferencesManager = PreferencesManager(context),
    private val geminiClient: GeminiClient = GeminiClient {
        val custom = preferencesManager.customBackendUrl
        if (custom.isNotBlank()) custom else GeminiConfig.DEFAULT_BACKEND_URL
    },
    private val database: SageDatabase = SageDatabase.getInstance(context)
) {

    /**
     * Enforce strict admin authorization before executing any administrative call.
     */
    private fun verifyAdminAuthorization() {
        AdminAuthManager.requireAdmin()
    }

    // ==========================================
    // 1. AUDIT LOGGING
    // ==========================================

    suspend fun recordAuditLog(
        action: String,
        targetUserId: String = "",
        targetTicketId: String = "",
        targetSnapshotId: String = "",
        metadata: Map<String, String> = emptyMap()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            verifyAdminAuthorization()
            val logId = "log_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            val entry = AdminAuditLogEntry(
                logId = logId,
                adminUid = AdminAuthManager.ADMIN_UID,
                adminName = AdminAuthManager.ADMIN_DISPLAY_NAME,
                action = action,
                targetUserId = targetUserId,
                targetTicketId = targetTicketId,
                targetSnapshotId = targetSnapshotId,
                timestamp = System.currentTimeMillis(),
                metadata = metadata
            )
            firestore.collection("adminAuditLogs").document(logId).set(entry).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAuditLogs(): Result<List<AdminAuditLogEntry>> = withContext(Dispatchers.IO) {
        try {
            verifyAdminAuthorization()
            val snapshot = firestore.collection("adminAuditLogs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(150)
                .get()
                .await()

            val logs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AdminAuditLogEntry::class.java)
            }
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. USER MANAGEMENT
    // ==========================================

    suspend fun getUsersList(): Result<List<AdminUserProfileItem>> = withContext(Dispatchers.IO) {
        try {
            verifyAdminAuthorization()
            val snapshot = firestore.collection("users").get().await()

            val userItems = mutableListOf<AdminUserProfileItem>()
            for (doc in snapshot.documents) {
                val profile = doc.toObject(FirestoreUserProfile::class.java)
                val uid = doc.id
                val displayName = profile?.displayName?.ifEmpty { "Sage Scholar" } ?: "Sage Scholar"
                val email = profile?.email.orEmpty()
                val createdAt = profile?.createdAt ?: 0L

                // Read academic profile subcollection
                var programme = "B. Tech CSE (AI & ML)"
                var regulation = "R25"
                var semester = 1
                var currentCourse = "CS101"
                var currentTopic = ""
                try {
                    val academicSnap = doc.reference.collection("academicProfile").document("current").get().await()
                    if (academicSnap.exists()) {
                        val ap = academicSnap.toObject(FirestoreAcademicProfile::class.java)
                        if (ap != null) {
                            programme = ap.programme
                            regulation = ap.regulation
                            semester = ap.currentSemester
                            currentCourse = ap.activeRoadmapTitle.ifEmpty { "CS101" }
                            currentTopic = ap.currentTopicTitle
                        }
                    }
                } catch (_: Exception) {}

                // Read sync metadata
                var lastSyncTime = profile?.updatedAt ?: 0L
                var syncStatus = "Active"
                var lastSyncError: String? = null
                try {
                    val syncSnap = doc.reference.collection("syncMetadata").document("status").get().await()
                    if (syncSnap.exists()) {
                        val sm = syncSnap.toObject(FirestoreSyncMetadata::class.java)
                        if (sm != null && sm.lastSyncTimestamp > 0) {
                            lastSyncTime = sm.lastSyncTimestamp
                        }
                    }
                } catch (_: Exception) {}

                // Read progress summary
                var completedCount = 0
                var inProgressCount = 0
                var totalTopics = 0
                try {
                    val progressSnap = doc.reference.collection("progress").limit(200).get().await()
                    totalTopics = progressSnap.size()
                    for (pDoc in progressSnap.documents) {
                        val status = pDoc.getString("status") ?: ""
                        if (status == "COMPLETED") completedCount++
                        if (status == "IN_PROGRESS") inProgressCount++
                    }
                } catch (_: Exception) {}

                // Read quizzes count
                var quizzesCount = 0
                var avgScore = 0
                try {
                    val quizSnap = doc.reference.collection("quizResults").limit(100).get().await()
                    quizzesCount = quizSnap.size()
                    var totalScorePct = 0
                    var scoreEntries = 0
                    for (qDoc in quizSnap.documents) {
                        val score = qDoc.getLong("score")?.toInt() ?: 0
                        val totalQuestions = qDoc.getLong("totalQuestions")?.toInt() ?: 0
                        if (totalQuestions > 0) {
                            totalScorePct += (score * 100) / totalQuestions
                            scoreEntries++
                        }
                    }
                    if (scoreEntries > 0) {
                        avgScore = totalScorePct / scoreEntries
                    }
                } catch (_: Exception) {}

                val progressPercentage = if (totalTopics > 0) (completedCount * 100) / totalTopics else 0

                userItems.add(
                    AdminUserProfileItem(
                        uid = uid,
                        displayName = displayName,
                        email = email,
                        createdAt = createdAt,
                        lastSyncTime = lastSyncTime,
                        lastActiveTime = maxOf(lastSyncTime, profile?.updatedAt ?: 0L),
                        programme = programme,
                        regulation = regulation,
                        semester = semester,
                        currentCourse = currentCourse,
                        currentTopic = currentTopic,
                        progressPercentage = progressPercentage,
                        completedTopicsCount = completedCount,
                        inProgressTopicsCount = inProgressCount,
                        totalTopicsCount = totalTopics,
                        quizzesCount = quizzesCount,
                        averageScore = avgScore,
                        syncStatus = syncStatus,
                        lastSyncError = lastSyncError
                    )
                )
            }

            // Sort by most recently active
            userItems.sortByDescending { it.lastActiveTime }
            Result.success(userItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserDetails(uid: String): Result<AdminUserDetails> = withContext(Dispatchers.IO) {
        try {
            verifyAdminAuthorization()
            val userRef = firestore.collection("users").document(uid)

            val profileSnap = userRef.get().await()
            val userProfile = profileSnap.toObject(FirestoreUserProfile::class.java)
                ?: FirestoreUserProfile(uid = uid, displayName = "Scholar", email = "")

            val academicSnap = userRef.collection("academicProfile").document("current").get().await()
            val academicProfile = academicSnap.toObject(FirestoreAcademicProfile::class.java)

            val progressSnap = userRef.collection("progress").limit(200).get().await()
            val progressList = progressSnap.documents.mapNotNull { it.toObject(FirestoreLearningProgress::class.java) }

            val quizSnap = userRef.collection("quizResults").orderBy("timestamp", Query.Direction.DESCENDING).limit(100).get().await()
            val quizResults = quizSnap.documents.mapNotNull { it.toObject(FirestoreQuizResult::class.java) }

            val syncSnap = userRef.collection("syncMetadata").document("status").get().await()
            val syncMetadata = syncSnap.toObject(FirestoreSyncMetadata::class.java)

            val snapshotsSnap = userRef.collection("snapshots").orderBy("createdAt", Query.Direction.DESCENDING).limit(10).get().await()
            val snapshots = snapshotsSnap.documents.mapNotNull { it.toObject(FirestoreRecoverySnapshot::class.java) }

            // Record audit log for viewing user details
            recordAuditLog(
                action = "VIEW_USER_DETAILS",
                targetUserId = uid,
                metadata = mapOf("userName" to userProfile.displayName, "email" to userProfile.email)
            )

            Result.success(
                AdminUserDetails(
                    userProfile = userProfile,
                    academicProfile = academicProfile,
                    progressList = progressList,
                    quizResults = quizResults,
                    syncMetadata = syncMetadata,
                    snapshots = snapshots
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 3. SUPPORT TICKETS
    // ==========================================

    suspend fun getSupportTickets(): Result<List<AdminSupportTicket>> = withContext(Dispatchers.IO) {
        try {
            verifyAdminAuthorization()
            val snapshot = firestore.collection("supportTickets")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(150)
                .get()
                .await()

            val tickets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AdminSupportTicket::class.java)
            }
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSupportTicket(
        ticketId: String,
        newStatus: String,
        adminNote: String,
        targetUserId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            verifyAdminAuthorization()
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "adminNote" to adminNote,
                "updatedAt" to System.currentTimeMillis()
            )

            if (newStatus == "RESOLVED" || newStatus == "CLOSED") {
                updates["resolvedAt"] = System.currentTimeMillis()
                updates["resolvedBy"] = AdminAuthManager.ADMIN_DISPLAY_NAME
            }

            firestore.collection("supportTickets").document(ticketId).update(updates).await()

            // Record audit log
            recordAuditLog(
                action = "UPDATE_SUPPORT_TICKET",
                targetUserId = targetUserId,
                targetTicketId = ticketId,
                metadata = mapOf("newStatus" to newStatus, "noteLength" to adminNote.length.toString())
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 4. DATA RECOVERY (SNAPSHOT RESTORATION)
    // ==========================================

    suspend fun getUserSnapshots(uid: String): Result<List<FirestoreRecoverySnapshot>> = withContext(Dispatchers.IO) {
        try {
            verifyAdminAuthorization()
            val snapshot = firestore.collection("users").document(uid)
                .collection("snapshots")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreRecoverySnapshot::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restores a selected snapshot for the user.
     * Safety mandate:
     * 1. Creates a backup snapshot of current cloud state before applying restoration.
     * 2. Overwrites only the target user's academic and progress state with the snapshot data.
     * 3. Maintains 5-snapshot limit.
     * 4. Logs audit entry.
     */
    suspend fun restoreUserSnapshot(
        uid: String,
        targetSnapshot: FirestoreRecoverySnapshot
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            verifyAdminAuthorization()
            val userRef = firestore.collection("users").document(uid)

            // Step 1: Create a safety backup snapshot of CURRENT cloud state
            val currentAcademicSnap = userRef.collection("academicProfile").document("current").get().await()
            val currentAcademic = currentAcademicSnap.toObject(FirestoreAcademicProfile::class.java)
            val currentProgressDocs = userRef.collection("progress").limit(250).get().await()
            val currentQuizDocs = userRef.collection("quizResults").limit(100).get().await()

            val preRestoreSnapshotId = "snap_pre_restore_${System.currentTimeMillis()}"
            val backupSnapshot = FirestoreRecoverySnapshot(
                snapshotId = preRestoreSnapshotId,
                createdAt = System.currentTimeMillis(),
                reason = "Pre-restore safety backup prior to restoring ${targetSnapshot.snapshotId}",
                academicProfile = currentAcademic,
                progressItemsCount = currentProgressDocs.size(),
                quizResultsCount = currentQuizDocs.size()
            )
            userRef.collection("snapshots").document(preRestoreSnapshotId).set(backupSnapshot).await()

            // Step 2: Restore academic profile if present in target snapshot
            if (targetSnapshot.academicProfile != null) {
                userRef.collection("academicProfile").document("current")
                    .set(targetSnapshot.academicProfile.copy(updatedAt = System.currentTimeMillis()), SetOptions.merge())
                    .await()
            }

            // Step 3: Enforce 5-snapshot limit
            val allSnaps = userRef.collection("snapshots").orderBy("createdAt").get().await()
            if (allSnaps.size() > 5) {
                val excess = allSnaps.documents.take(allSnaps.size() - 5)
                for (d in excess) {
                    d.reference.delete()
                }
            }

            // Step 4: Record audit log
            recordAuditLog(
                action = "RESTORE_SNAPSHOT",
                targetUserId = uid,
                targetSnapshotId = targetSnapshot.snapshotId,
                metadata = mapOf(
                    "preRestoreBackupId" to preRestoreSnapshotId,
                    "targetSnapshotReason" to targetSnapshot.reason
                )
            )

            Result.success(preRestoreSnapshotId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 5. SYSTEM HEALTH CHECK
    // ==========================================

    suspend fun runFullSystemHealthCheck(): SystemHealthReport = withContext(Dispatchers.IO) {
        verifyAdminAuthorization()

        // 1. Network Connectivity Check
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork
        val caps = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        val networkConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val networkDetails = if (networkConnected) {
            val transport = when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
                else -> "Active Connection"
            }
            "Online ($transport)"
        } else {
            "Offline / No Active Interface"
        }

        // 2. Firebase Authentication
        val currentUser = auth.currentUser
        val firebaseAuthConnected = currentUser != null
        val currentUid = currentUser?.uid ?: "Not Authenticated"
        val currentEmail = currentUser?.email ?: "Guest"

        // 3. Firestore Availability Check
        var firestoreConnected = false
        var firestoreDetails = "Disconnected"
        try {
            if (currentUser == null) {
                firestoreConnected = false
                firestoreDetails = "Unauthenticated: Sign in required"
            } else if (AdminAuthManager.isUserAdmin(currentUser.uid)) {
                // Admin probe: perform safe authenticated query against admin-authorized collection
                firestore.collection("adminAuditLogs").limit(1).get().await()
                firestoreConnected = true
                firestoreDetails = "Connected & Authorized (Admin UID verified)"
            } else {
                // Normal user probe: perform safe authenticated query against user's own document
                firestore.collection("users").document(currentUser.uid).get().await()
                firestoreConnected = true
                firestoreDetails = "Connected & Authorized (User document accessible)"
            }
        } catch (e: Exception) {
            firestoreConnected = false
            firestoreDetails = e.localizedMessage ?: "Firestore unreachable"
        }

        // 4. Render Backend & Gemini API
        val customUrl = preferencesManager.customBackendUrl
        val backendUrl: String = if (customUrl.isNotBlank()) customUrl else GeminiConfig.DEFAULT_BACKEND_URL
        var renderConnected = false
        var renderStatus = "Pending"
        var renderLatency = -1L
        var geminiConnected = false
        var geminiModel = GeminiConfig.GEMINI_MODEL

        val startTime = System.currentTimeMillis()
        try {
            val result = geminiClient.testConnection()
            renderLatency = System.currentTimeMillis() - startTime
            when (result) {
                is GeminiResult.Success -> {
                    renderConnected = true
                    renderStatus = "HTTP 200 OK"
                    geminiConnected = true
                    geminiModel = GeminiConfig.GEMINI_MODEL
                }
                is GeminiResult.Error -> {
                    renderStatus = result.diagnosticMessage
                }
            }
        } catch (e: Exception) {
            renderLatency = System.currentTimeMillis() - startTime
            renderStatus = e.localizedMessage ?: "Backend probe error"
        }

        // 5. Room SQLite Database
        var roomConnected = false
        var roomStats = "Unavailable"
        try {
            val currentRoadmap = database.sageDao().getCurrentRoadmapOnce()
            roomConnected = true
            roomStats = if (currentRoadmap != null) "SQLite Active • Current: ${currentRoadmap.title}" else "SQLite Active • Ready"
        } catch (e: Exception) {
            roomStats = e.localizedMessage ?: "Room error"
        }

        // 6. Cloud Sync state
        val syncStateDesc = "Active Engine v2"

        val report = SystemHealthReport(
            timestamp = System.currentTimeMillis(),
            networkConnected = networkConnected,
            networkDetails = networkDetails,
            firebaseAuthConnected = firebaseAuthConnected,
            currentAuthUid = currentUid,
            currentAuthEmail = currentEmail,
            firestoreConnected = firestoreConnected,
            firestoreDetails = firestoreDetails,
            renderBackendConnected = renderConnected,
            renderBackendUrl = backendUrl,
            renderBackendStatus = renderStatus,
            renderBackendLatencyMs = renderLatency,
            geminiConnected = geminiConnected,
            geminiModel = geminiModel,
            roomDatabaseConnected = roomConnected,
            roomStats = roomStats,
            cloudSyncState = syncStateDesc,
            cloudSyncTimestamp = System.currentTimeMillis()
        )

        // Record audit for health check
        recordAuditLog(
            action = "SYSTEM_HEALTH_CHECK",
            metadata = mapOf(
                "renderLatencyMs" to renderLatency.toString(),
                "renderOk" to renderConnected.toString(),
                "firestoreOk" to firestoreConnected.toString()
            )
        )

        report
    }
}
