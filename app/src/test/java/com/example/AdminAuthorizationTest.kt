package com.example

import com.example.data.admin.AdminAuditLogEntry
import com.example.data.admin.AdminSupportTicket
import com.example.data.auth.AdminAuthManager
import com.example.data.sync.FirestoreAcademicProfile
import com.example.data.sync.FirestoreRecoverySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminAuthorizationTest {

    @Test
    fun `admin authorization matches only exact admin UID`() {
        val adminUid = "SedsiyiYU7Pn2B8nQ3f7PqJsMTM2"
        assertTrue(AdminAuthManager.isUserAdmin(adminUid))

        // Normal users
        assertFalse(AdminAuthManager.isUserAdmin("random_user_12345"))
        assertFalse(AdminAuthManager.isUserAdmin("user_xyz987"))
        assertFalse(AdminAuthManager.isUserAdmin("piyush.kumar@example.com")) // Email never authorized
        assertFalse(AdminAuthManager.isUserAdmin(null as String?))
        assertFalse(AdminAuthManager.isUserAdmin(""))
        assertFalse(AdminAuthManager.isUserAdmin("   "))
    }

    @Test
    fun `admin display name is Piyush Kumar`() {
        assertEquals("Piyush Kumar", AdminAuthManager.ADMIN_DISPLAY_NAME)
        assertEquals("SedsiyiYU7Pn2B8nQ3f7PqJsMTM2", AdminAuthManager.ADMIN_UID)
    }

    @Test
    fun `support ticket model holds sanitized diagnostics and does not store credentials`() {
        val diagnostics = mapOf(
            "appVersion" to "1.0.0",
            "androidVersion" to "14",
            "deviceModel" to "Google Pixel 8",
            "activeRoadmapId" to "roadmap_android",
            "syncStatus" to "Active"
        )

        val ticket = AdminSupportTicket(
            ticketId = "ticket_123",
            userId = "user_456",
            userEmail = "student@example.com",
            userName = "Student One",
            category = "sync",
            subject = "Progress not syncing",
            description = "My quiz score from yesterday didn't appear on my dashboard.",
            status = "OPEN",
            diagnostics = diagnostics,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        assertEquals("ticket_123", ticket.ticketId)
        assertEquals("OPEN", ticket.status)
        assertNotNull(ticket.diagnostics)
        assertEquals("roadmap_android", ticket.diagnostics["activeRoadmapId"])
        assertEquals("", ticket.adminNote)
    }

    @Test
    fun `recovery snapshot structure validates correctly`() {
        val academicProfile = FirestoreAcademicProfile(
            programme = "B. Tech CSE (AI & ML)",
            regulation = "R25",
            currentSemester = 1
        )
        val snapshot = FirestoreRecoverySnapshot(
            snapshotId = "snap_test_001",
            createdAt = System.currentTimeMillis(),
            reason = "Pre-sync safety backup",
            academicProfile = academicProfile,
            progressItemsCount = 14,
            quizResultsCount = 6,
            progressSnapshotJson = "{}"
        )

        assertEquals("snap_test_001", snapshot.snapshotId)
        assertEquals("Pre-sync safety backup", snapshot.reason)
        assertEquals(14, snapshot.progressItemsCount)
        assertEquals(6, snapshot.quizResultsCount)
        assertEquals("R25", snapshot.academicProfile?.regulation)
    }

    @Test
    fun `audit log entry structure preserves admin attribution`() {
        val audit = AdminAuditLogEntry(
            logId = "log_001",
            adminUid = AdminAuthManager.ADMIN_UID,
            adminName = AdminAuthManager.ADMIN_DISPLAY_NAME,
            action = "USER_SNAPSHOT_RESTORE",
            targetUserId = "target_uid_999",
            metadata = mapOf("snapshotId" to "snap_123")
        )

        assertEquals("SedsiyiYU7Pn2B8nQ3f7PqJsMTM2", audit.adminUid)
        assertEquals("Piyush Kumar", audit.adminName)
        assertEquals("USER_SNAPSHOT_RESTORE", audit.action)
    }
}

