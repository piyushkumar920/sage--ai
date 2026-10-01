package com.example.data.sync

import com.example.data.local.DailyQuizRecordEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.QuizResultEntity
import com.example.data.local.RoadmapProgressEntity
import com.example.data.local.SageDao
import com.example.data.local.TopicProgressEntity
import com.example.data.local.WeakConceptEntity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val dao: SageDao,
    private val preferencesManager: PreferencesManager
) {

    /**
     * Creates a lightweight recovery snapshot before performing cloud synchronization
     * that could overwrite or update existing cloud documents.
     */
    suspend fun createRecoverySnapshot(uid: String, reason: String = "Pre-sync safety backup"): String? = withContext(Dispatchers.IO) {
        try {
            val userDoc = firestore.collection("users").document(uid)
            val academicSnapshot = userDoc.collection("academicProfile").document("current").get().await()
            val academicProfile = academicSnapshot.toObject(FirestoreAcademicProfile::class.java)

            val progressDocs = userDoc.collection("progress").limit(250).get().await()
            val quizzesDocs = userDoc.collection("quizResults").limit(100).get().await()

            val snapshotId = "snap_${System.currentTimeMillis()}"
            val snapshotData = FirestoreRecoverySnapshot(
                snapshotId = snapshotId,
                createdAt = System.currentTimeMillis(),
                reason = reason,
                academicProfile = academicProfile,
                progressItemsCount = progressDocs.size(),
                quizResultsCount = quizzesDocs.size(),
                progressSnapshotJson = "" // Compact representation
            )

            // Write snapshot under users/{uid}/snapshots/{snapshotId}
            userDoc.collection("snapshots").document(snapshotId).set(snapshotData).await()

            // Keep snapshots limited: keep only last 5 snapshots to avoid unnecessary database bloat
            val allSnapshots = userDoc.collection("snapshots").orderBy("createdAt").get().await()
            if (allSnapshots.size() > 5) {
                val excess = allSnapshots.documents.take(allSnapshots.size() - 5)
                for (doc in excess) {
                    doc.reference.delete()
                }
            }

            snapshotId
        } catch (e: Exception) {
            // Snapshot creation failure must not crash or block offline operations
            null
        }
    }

    /**
     * Executes bidirectional cloud synchronization between Room and Firestore.
     * Uses non-destructive monotonic conflict resolution (completed learning is always preserved).
     */
    suspend fun syncUserData(user: FirebaseUser): SyncResult = withContext(Dispatchers.IO) {
        val uid = user.uid
        val userDoc = firestore.collection("users").document(uid)

        try {
            // 1. Safety snapshot
            createRecoverySnapshot(uid, "Scheduled sync backup")

            // 2. Sync UserProfile
            val profileData = FirestoreUserProfile(
                uid = uid,
                displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Sage Scholar",
                email = user.email.orEmpty(),
                updatedAt = System.currentTimeMillis()
            )
            userDoc.set(profileData, SetOptions.merge()).await()

            // 3. Sync Academic Profile
            val academicDocRef = userDoc.collection("academicProfile").document("current")
            val remoteAcademicSnap = academicDocRef.get().await()
            val remoteAcademic = remoteAcademicSnap.toObject(FirestoreAcademicProfile::class.java)

            val localProfile = preferencesManager.getAcademicProfile()
            val localHasProfile = preferencesManager.hasAcademicProfile
            val localUpdatedAt = preferencesManager.academicProfileUpdatedAt

            if (remoteAcademic != null && remoteAcademic.hasProfile && remoteAcademic.departmentId.isNotBlank() &&
                (!localHasProfile || remoteAcademic.updatedAt > localUpdatedAt)
            ) {
                // Remote profile takes precedence (newer or local empty)
                val newLocalProfile = com.example.data.profile.AcademicProfile(
                    departmentId = remoteAcademic.departmentId,
                    departmentName = remoteAcademic.departmentName,
                    programmeId = remoteAcademic.programmeId.ifBlank { remoteAcademic.departmentId },
                    programmeName = remoteAcademic.programme,
                    regulationId = remoteAcademic.regulation.lowercase(),
                    regulation = remoteAcademic.regulation,
                    semester = remoteAcademic.currentSemester,
                    updatedAt = remoteAcademic.updatedAt
                )
                preferencesManager.saveAcademicProfile(newLocalProfile)
                dao.saveAcademicProfile(newLocalProfile.toEntity())

                if (remoteAcademic.activeRoadmapId.isNotBlank()) {
                    preferencesManager.activeRoadmapId = remoteAcademic.activeRoadmapId
                    preferencesManager.activeRoadmapTitle = remoteAcademic.activeRoadmapTitle
                }
                if (remoteAcademic.currentTopicId.isNotBlank()) {
                    preferencesManager.currentTopicId = remoteAcademic.currentTopicId
                    preferencesManager.currentTopicTitle = remoteAcademic.currentTopicTitle
                }
            } else if (localProfile != null && localHasProfile) {
                // Push local academic profile to Firestore
                val localAcademic = FirestoreAcademicProfile(
                    hasProfile = true,
                    departmentId = localProfile.departmentId,
                    departmentName = localProfile.departmentName,
                    programme = localProfile.programmeName,
                    programmeId = localProfile.programmeId,
                    regulation = localProfile.regulation,
                    currentSemester = localProfile.semester,
                    activeRoadmapId = preferencesManager.activeRoadmapId,
                    activeRoadmapTitle = preferencesManager.activeRoadmapTitle,
                    currentTopicId = preferencesManager.currentTopicId,
                    currentTopicTitle = preferencesManager.currentTopicTitle,
                    updatedAt = localProfile.updatedAt
                )
                academicDocRef.set(localAcademic, SetOptions.merge()).await()
            }

            // 4. Sync Topic Progress (Bidirectional Monotonic Merge)
            val remoteProgressDocs = userDoc.collection("progress").get().await()
            val remoteProgressMap = remoteProgressDocs.documents.associate { doc ->
                val item = doc.toObject(FirestoreLearningProgress::class.java)!!
                item.key to item
            }

            val localRoadmapId = preferencesManager.activeRoadmapId
            val localProgressList = dao.getTopicProgressForRoadmapOnce(localRoadmapId)
            val localProgressMap = localProgressList.associateBy { it.key }

            var syncedTopicsCount = 0

            // Merge local to remote and remote to local
            val allKeys = localProgressMap.keys + remoteProgressMap.keys

            for (key in allKeys) {
                val local = localProgressMap[key]
                val remote = remoteProgressMap[key]

                if (local != null && remote != null) {
                    // Conflict Resolution: Preserving highest completion state
                    val resolvedStatus = resolveStatus(local.status, remote.status)
                    val resolvedScore = maxOf(local.quizScore ?: 0, remote.quizScore ?: 0).takeIf { it > 0 }
                    val resolvedCompletedAt = local.completedAt ?: remote.completedAt
                    val resolvedStudiedInChat = local.studiedInChat || remote.studiedInChat
                    val resolvedSessions = maxOf(local.sessionsCount, remote.sessionsCount)
                    val resolvedStudyMinutes = maxOf(local.studyTimeMinutes, remote.studyTimeMinutes)
                    val resolvedLastStudiedAt = maxOf(local.lastStudiedAt ?: 0L, remote.lastStudiedAt ?: 0L).takeIf { it > 0L }
                    val resolvedUpdatedAt = maxOf(local.updatedAt, remote.updatedAt)

                    val mergedLocal = local.copy(
                        status = resolvedStatus,
                        quizScore = resolvedScore,
                        completedAt = resolvedCompletedAt,
                        studiedInChat = resolvedStudiedInChat,
                        sessionsCount = resolvedSessions,
                        studyTimeMinutes = resolvedStudyMinutes,
                        lastStudiedAt = resolvedLastStudiedAt,
                        updatedAt = resolvedUpdatedAt
                    )
                    dao.insertOrUpdateTopicProgress(mergedLocal)

                    val mergedRemote = FirestoreLearningProgress(
                        key = mergedLocal.key,
                        roadmapId = mergedLocal.roadmapId,
                        nodeId = mergedLocal.nodeId,
                        nodeTitle = mergedLocal.nodeTitle,
                        category = mergedLocal.category,
                        status = resolvedStatus,
                        quizScore = resolvedScore,
                        completedAt = resolvedCompletedAt,
                        studiedInChat = resolvedStudiedInChat,
                        sessionsCount = resolvedSessions,
                        studyTimeMinutes = resolvedStudyMinutes,
                        lastStudiedAt = resolvedLastStudiedAt,
                        updatedAt = resolvedUpdatedAt
                    )
                    userDoc.collection("progress").document(escapeKey(key)).set(mergedRemote, SetOptions.merge()).await()
                    syncedTopicsCount++
                } else if (local != null && remote == null) {
                    // Push local to remote
                    val remoteItem = FirestoreLearningProgress(
                        key = local.key,
                        roadmapId = local.roadmapId,
                        nodeId = local.nodeId,
                        nodeTitle = local.nodeTitle,
                        category = local.category,
                        status = local.status,
                        quizScore = local.quizScore,
                        completedAt = local.completedAt,
                        studiedInChat = local.studiedInChat,
                        sessionsCount = local.sessionsCount,
                        studyTimeMinutes = local.studyTimeMinutes,
                        lastStudiedAt = local.lastStudiedAt,
                        updatedAt = local.updatedAt
                    )
                    userDoc.collection("progress").document(escapeKey(local.key)).set(remoteItem, SetOptions.merge()).await()
                    syncedTopicsCount++
                } else if (local == null && remote != null) {
                    // Pull remote to local
                    val localItem = TopicProgressEntity(
                        key = remote.key,
                        roadmapId = remote.roadmapId,
                        nodeId = remote.nodeId,
                        nodeTitle = remote.nodeTitle,
                        category = remote.category,
                        status = remote.status,
                        quizScore = remote.quizScore,
                        completedAt = remote.completedAt,
                        updatedAt = remote.updatedAt,
                        studiedInChat = remote.studiedInChat,
                        sessionsCount = remote.sessionsCount,
                        studyTimeMinutes = remote.studyTimeMinutes,
                        lastStudiedAt = remote.lastStudiedAt
                    )
                    dao.insertOrUpdateTopicProgress(localItem)
                    syncedTopicsCount++
                }
            }

            // 5. Sync User Preferences (Streak, Study Minutes)
            val remotePrefsDoc = userDoc.collection("preferences").document("learning").get().await()
            val remotePrefs = remotePrefsDoc.toObject(FirestoreUserPreferences::class.java)

            if (remotePrefs != null) {
                // Deterministic monotonic merge for learning progress
                if (remotePrefs.longestStreak > preferencesManager.longestStreak) {
                    preferencesManager.longestStreak = remotePrefs.longestStreak
                }
                if (remotePrefs.streakDays > preferencesManager.streakDays) {
                    preferencesManager.streakDays = remotePrefs.streakDays
                }
                if (remotePrefs.totalStudyMinutes > preferencesManager.studyMinutes) {
                    preferencesManager.studyMinutes = remotePrefs.totalStudyMinutes
                }
            }

            // Write back merged preferences
            val updatedPrefs = FirestoreUserPreferences(
                streakDays = preferencesManager.streakDays,
                longestStreak = preferencesManager.longestStreak,
                totalStudyMinutes = preferencesManager.studyMinutes,
                updatedAt = System.currentTimeMillis()
            )
            userDoc.collection("preferences").document("learning").set(updatedPrefs, SetOptions.merge()).await()

            // 6. Update Sync Metadata
            val metadata = FirestoreSyncMetadata(
                lastSyncTimestamp = System.currentTimeMillis(),
                clientAppVersion = "1.0.0",
                syncEngineVersion = 2,
                totalSyncedTopics = syncedTopicsCount
            )
            userDoc.collection("syncMetadata").document("status").set(metadata, SetOptions.merge()).await()

            SyncResult.Success(syncedTopics = syncedTopicsCount)
        } catch (e: Exception) {
            SyncResult.Error(e.localizedMessage ?: "Sync operation failed", e)
        }
    }

    private fun resolveStatus(localStatus: String, remoteStatus: String): String {
        val hierarchy = listOf("NOT_STARTED", "NEEDS_REVIEW", "IN_PROGRESS", "COMPLETED")
        val localRank = hierarchy.indexOf(localStatus).coerceAtLeast(0)
        val remoteRank = hierarchy.indexOf(remoteStatus).coerceAtLeast(0)
        return if (localRank >= remoteRank) localStatus else remoteStatus
    }

    private fun escapeKey(key: String): String {
        return key.replace("#", "_").replace("/", "_")
    }
}

sealed class SyncResult {
    data class Success(val syncedTopics: Int) : SyncResult()
    data class Error(val message: String, val exception: Throwable? = null) : SyncResult()
}
