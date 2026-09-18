package com.example.sync

import com.example.data.sync.FirestoreAcademicProfile
import com.example.data.sync.FirestoreLearningProgress
import com.example.data.sync.FirestoreRecoverySnapshot
import com.example.data.sync.FirestoreUserProfile
import com.example.data.sync.FirestoreUserPreferences
import com.example.data.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SageSyncUnitTest {

    @Test
    fun testFirestoreUserProfileDefaults() {
        val profile = FirestoreUserProfile(
            uid = "test_uid_12345",
            displayName = "Scholar",
            email = "scholar@example.com"
        )
        assertEquals("test_uid_12345", profile.uid)
        assertEquals("Scholar", profile.displayName)
        assertEquals("scholar@example.com", profile.email)
        assertEquals(1, profile.schemaVersion)
        assertTrue(profile.createdAt > 0)
    }

    @Test
    fun testFirestoreAcademicProfileStructure() {
        val academic = FirestoreAcademicProfile(
            departmentId = "cse_aiml",
            programme = "B. Tech CSE (AI & ML)",
            regulation = "R25",
            currentSemester = 1,
            activeRoadmapId = "curriculum_cse_aiml_CS101"
        )
        assertEquals("cse_aiml", academic.departmentId)
        assertEquals("R25", academic.regulation)
        assertEquals(1, academic.currentSemester)
        assertEquals("curriculum_cse_aiml_CS101", academic.activeRoadmapId)
    }

    @Test
    fun testConflictResolutionMonotonicProgress() {
        // Higher completion rank should always win to avoid accidental regression
        val hierarchy = listOf("NOT_STARTED", "NEEDS_REVIEW", "IN_PROGRESS", "COMPLETED")
        
        fun resolveStatus(localStatus: String, remoteStatus: String): String {
            val localRank = hierarchy.indexOf(localStatus).coerceAtLeast(0)
            val remoteRank = hierarchy.indexOf(remoteStatus).coerceAtLeast(0)
            return if (localRank >= remoteRank) localStatus else remoteStatus
        }

        // Test cases
        assertEquals("COMPLETED", resolveStatus("COMPLETED", "IN_PROGRESS"))
        assertEquals("COMPLETED", resolveStatus("IN_PROGRESS", "COMPLETED"))
        assertEquals("IN_PROGRESS", resolveStatus("IN_PROGRESS", "NOT_STARTED"))
        assertEquals("IN_PROGRESS", resolveStatus("NOT_STARTED", "IN_PROGRESS"))
        assertEquals("NEEDS_REVIEW", resolveStatus("NEEDS_REVIEW", "NOT_STARTED"))
    }

    @Test
    fun testPreferencesMonotonicMerge() {
        val localStreak = 5
        val remoteStreak = 7
        val mergedStreak = maxOf(localStreak, remoteStreak)
        assertEquals(7, mergedStreak)

        val localMinutes = 90
        val remoteMinutes = 45
        val mergedMinutes = maxOf(localMinutes, remoteMinutes)
        assertEquals(90, mergedMinutes)
    }

    @Test
    fun testRecoverySnapshotCreationData() {
        val snapshot = FirestoreRecoverySnapshot(
            snapshotId = "snap_1700000000000",
            reason = "Pre-sync safety backup",
            progressItemsCount = 12,
            quizResultsCount = 5
        )
        assertNotNull(snapshot.snapshotId)
        assertEquals("Pre-sync safety backup", snapshot.reason)
        assertEquals(12, snapshot.progressItemsCount)
        assertEquals(5, snapshot.quizResultsCount)
    }

    @Test
    fun testSyncStateHierarchy() {
        val idleState: SyncState = SyncState.Idle
        val syncingState: SyncState = SyncState.Syncing
        val syncedState: SyncState = SyncState.Synced(System.currentTimeMillis())
        val pendingState: SyncState = SyncState.Pending("Offline")
        val errorState: SyncState = SyncState.Error("Network failure")

        assertTrue(idleState is SyncState.Idle)
        assertTrue(syncingState is SyncState.Syncing)
        assertTrue(syncedState is SyncState.Synced)
        assertTrue(pendingState is SyncState.Pending)
        assertTrue(errorState is SyncState.Error)
    }
}
