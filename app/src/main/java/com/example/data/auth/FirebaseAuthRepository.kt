package com.example.data.auth

import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : AuthResult<Nothing>()
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val isUserAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null

    /**
     * Observable flow of current Firebase User authentication state.
     * Automatically emits updates on sign in, sign out, or token changes.
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        // Emit initial value
        trySend(firebaseAuth.currentUser)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    /**
     * Sign in with Email and Password using Firebase Auth.
     * Never persists or logs credentials.
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Authentication succeeded but user profile is null.")
            }
        } catch (e: Throwable) {
            AuthResult.Error(getReadableAuthErrorMessage(e), e)
        }
    }

    /**
     * Create account with Full Name, Email, and Password using Firebase Auth.
     * Sets display name upon successful creation.
     */
    suspend fun createAccount(name: String, email: String, password: String): AuthResult<UserProfile> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return AuthResult.Error("Account creation failed: User is null.")

            // Set the Firebase user's display name
            val profileUpdates = userProfileChangeRequest {
                displayName = name.trim()
            }
            user.updateProfile(profileUpdates).await()

            // Prepare UserProfile model (Phase 1 preparation - no Firestore write)
            val profile = UserProfile(
                uid = user.uid,
                displayName = name.trim().ifEmpty { user.email?.substringBefore("@") ?: "Sage Scholar" },
                email = user.email.orEmpty(),
                createdAt = System.currentTimeMillis()
            )

            AuthResult.Success(profile)
        } catch (e: Throwable) {
            AuthResult.Error(getReadableAuthErrorMessage(e), e)
        }
    }

    /**
     * Send password reset email via Firebase Auth.
     */
    suspend fun sendPasswordReset(email: String): AuthResult<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(Unit)
        } catch (e: Throwable) {
            AuthResult.Error(getReadableAuthErrorMessage(e), e)
        }
    }

    /**
     * Signs out current user from Firebase Auth.
     */
    fun signOut() {
        try {
            firebaseAuth.signOut()
        } catch (_: Throwable) {}
    }

    private fun getReadableAuthErrorMessage(e: Throwable): String {
        // 1. Direct Firebase Auth exception checks
        if (e is FirebaseAuthInvalidCredentialsException) {
            return "Incorrect email or password. If you don't have an account yet, please tap 'Create Account' below to sign up."
        }
        if (e is FirebaseAuthInvalidUserException) {
            return "No account found with this email. Please check your email or tap 'Create Account' to sign up."
        }
        if (e is FirebaseAuthUserCollisionException) {
            return "This email address is already registered. Please log in or reset your password."
        }
        if (e is FirebaseAuthWeakPasswordException) {
            return "Password is too weak. Please use at least 6 characters."
        }

        // 2. Firebase Auth error codes (standard Identity Platform codes)
        if (e is FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL",
                "ERROR_WRONG_PASSWORD",
                "INVALID_LOGIN_CREDENTIALS",
                "auth/invalid-credential",
                "auth/wrong-password" ->
                    return "Incorrect email or password. If you don't have an account yet, please tap 'Create Account' below to sign up."

                "ERROR_USER_NOT_FOUND",
                "auth/user-not-found" ->
                    return "No account found with this email. Please check your email or tap 'Create Account' to sign up."

                "ERROR_USER_DISABLED",
                "auth/user-disabled" ->
                    return "This account has been disabled. Please contact support."

                "ERROR_EMAIL_ALREADY_IN_USE",
                "auth/email-already-in-use" ->
                    return "This email address is already registered. Please log in instead or reset your password."

                "ERROR_INVALID_EMAIL",
                "auth/invalid-email" ->
                    return "Please enter a valid email address."

                "ERROR_WEAK_PASSWORD",
                "auth/weak-password" ->
                    return "Password is too weak. Please use at least 6 characters."

                "ERROR_TOO_MANY_REQUESTS",
                "auth/too-many-requests" ->
                    return "Too many failed attempts. Please wait a few moments and try again."

                "ERROR_NETWORK_REQUEST_FAILED",
                "auth/network-request-failed" ->
                    return "Network connection error. Please check your internet connection."

                "ERROR_OPERATION_NOT_ALLOWED",
                "auth/operation-not-allowed" ->
                    return "Email/Password sign-in is not enabled. Please contact administrator."
            }
        }

        // 3. Fallback text matching for error responses and internal Recaptcha wrappers
        val msg = e.message.orEmpty()
        return when {
            msg.contains("The email address is badly formatted", ignoreCase = true) ||
            msg.contains("ERROR_INVALID_EMAIL", ignoreCase = true) ||
            msg.contains("invalid-email", ignoreCase = true) ->
                "Please enter a valid email address."

            msg.contains("There is no user record", ignoreCase = true) ||
            msg.contains("ERROR_USER_NOT_FOUND", ignoreCase = true) ||
            msg.contains("user-not-found", ignoreCase = true) ->
                "No account found with this email. Please check or create an account."

            msg.contains("supplied auth credential is incorrect", ignoreCase = true) ||
            msg.contains("malformed or has expired", ignoreCase = true) ||
            msg.contains("The password is invalid", ignoreCase = true) ||
            msg.contains("wrong-password", ignoreCase = true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("INVALID_CREDENTIAL", ignoreCase = true) ||
            msg.contains("invalid-credential", ignoreCase = true) ->
                "Incorrect email or password. If you don't have an account yet, please tap 'Create Account' below to sign up."

            msg.contains("The email address is already in use", ignoreCase = true) ||
            msg.contains("email-already-in-use", ignoreCase = true) ||
            msg.contains("EMAIL_EXISTS", ignoreCase = true) ->
                "This email address is already registered. Try logging in instead."

            msg.contains("The given password is invalid", ignoreCase = true) ||
            msg.contains("weak-password", ignoreCase = true) ||
            msg.contains("WEAK_PASSWORD", ignoreCase = true) ->
                "Password is too weak. Please use at least 6 characters."

            msg.contains("network error", ignoreCase = true) ||
            msg.contains("NETWORK_ERROR", ignoreCase = true) ||
            msg.contains("network-request-failed", ignoreCase = true) ->
                "Network connection error. Please check your internet connection."

            msg.contains("too many requests", ignoreCase = true) ||
            msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) ||
            msg.contains("too-many-requests", ignoreCase = true) ->
                "Too many attempts. Please wait a few moments and try again."

            else -> "Incorrect email or password. Please verify your credentials or tap 'Create Account' to sign up."
        }
    }
}
