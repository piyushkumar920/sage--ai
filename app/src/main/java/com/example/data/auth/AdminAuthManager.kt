package com.example.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for Sage Administrator Authorization.
 *
 * Security Mandate:
 * - Admin authorization is strictly evaluated via Firebase Authentication's currentUser.uid.
 * - The account is an admin ONLY when:
 *     FirebaseAuth.currentUser?.uid == "SedsiyiYU7Pn2B8nQ3f7PqJsMTM2"
 * - Belongs to administrator: Piyush Kumar.
 * - Email address, client parameters, passwords, or guest state NEVER grant admin access.
 */
object AdminAuthManager {

    /**
     * Dedicated Firebase UID of the Sage Administrator (Piyush Kumar).
     */
    const val ADMIN_UID = "SedsiyiYU7Pn2B8nQ3f7PqJsMTM2"

    /**
     * Administrator display name.
     */
    const val ADMIN_DISPLAY_NAME = "Piyush Kumar"

    /**
     * Checks if the currently signed-in Firebase user is the authorized administrator.
     * Unauthenticated and guest users return false.
     */
    fun isAdmin(): Boolean {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        return isUserAdmin(currentUid)
    }

    /**
     * Checks if a given FirebaseUser is the authorized administrator.
     */
    fun isUserAdmin(user: FirebaseUser?): Boolean {
        return isUserAdmin(user?.uid)
    }

    /**
     * Pure UID verification check.
     */
    fun isUserAdmin(uid: String?): Boolean {
        if (uid.isNullOrBlank()) return false
        return uid == ADMIN_UID
    }

    /**
     * Security gate assertion: Throws SecurityException if the caller is not the admin.
     */
    @Throws(SecurityException::class)
    fun requireAdmin() {
        if (!isAdmin()) {
            throw SecurityException("Access Denied: Action requires Sage Administrator authorization.")
        }
    }

    /**
     * Observes whether the currently signed-in user has admin privileges.
     */
    fun observeIsAdmin(authRepository: FirebaseAuthRepository): Flow<Boolean> {
        return authRepository.authStateFlow.map { user ->
            isUserAdmin(user?.uid)
        }
    }
}
