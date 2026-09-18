package com.example.data.support

import android.content.Context
import android.os.Build
import com.example.data.admin.AdminSupportTicket
import com.example.data.local.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class SupportTicketService(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val preferencesManager: PreferencesManager = PreferencesManager(context)
) {

    /**
     * Gathers safe system diagnostics.
     * STRICT PRIVACY MANDATE:
     * Never gathers passwords, tokens, API keys, credentials, or private message texts.
     */
    fun collectSafeDiagnostics(
        backendStatus: String = "Online",
        syncStatus: String = "Active",
        lastErrorCode: String? = null
    ): Map<String, String> {
        return mapOf(
            "appVersion" to "1.0.0",
            "androidVersion" to Build.VERSION.RELEASE,
            "sdkInt" to Build.VERSION.SDK_INT.toString(),
            "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "selectedProgramme" to "B. Tech CSE (AI & ML)",
            "currentSemester" to "1",
            "activeRoadmapId" to preferencesManager.activeRoadmapId,
            "syncStatus" to syncStatus,
            "backendStatus" to backendStatus,
            "lastKnownErrorCode" to (lastErrorCode ?: "NONE")
        )
    }

    /**
     * Submits a support ticket to Firestore supportTickets/{ticketId}.
     * If user is unauthenticated (guest), returns error requiring authentication to submit cloud tickets,
     * maintaining strict Spark/Firestore security rules.
     */
    suspend fun submitTicket(
        category: String,
        subject: String,
        description: String,
        includeDiagnostics: Boolean,
        backendStatus: String = "Online",
        syncStatus: String = "Active",
        lastErrorCode: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val user = auth.currentUser
        if (user == null) {
            return@withContext Result.failure(
                IllegalStateException("Please sign in or create an account to submit support tickets so our team can follow up with you.")
            )
        }

        try {
            val ticketId = "tkt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            val safeDiagnostics = if (includeDiagnostics) {
                collectSafeDiagnostics(backendStatus, syncStatus, lastErrorCode)
            } else {
                emptyMap()
            }

            val ticket = AdminSupportTicket(
                ticketId = ticketId,
                userId = user.uid,
                userName = user.displayName?.ifEmpty { user.email?.substringBefore("@") } ?: "Sage Scholar",
                userEmail = user.email.orEmpty(),
                category = category,
                subject = subject.trim(),
                description = description.trim(),
                diagnostics = safeDiagnostics,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                status = "OPEN",
                adminNote = "",
                resolvedAt = null,
                resolvedBy = null
            )

            firestore.collection("supportTickets").document(ticketId).set(ticket).await()
            Result.success(ticketId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
