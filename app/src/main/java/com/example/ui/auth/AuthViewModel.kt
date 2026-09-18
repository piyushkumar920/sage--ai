package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthResult
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isActionSuccessful: Boolean = false
)

class AuthViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val syncManager: com.example.data.sync.SageSyncManager? = null
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.currentUser)

    val isUserLoggedIn: Boolean
        get() = authRepository.isUserAuthenticated

    val isAdmin: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(currentUser, kotlinx.coroutines.flow.flowOf(Unit)) { user, _ ->
        com.example.data.auth.AdminAuthManager.isUserAdmin(user?.uid)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, com.example.data.auth.AdminAuthManager.isUserAdmin(authRepository.currentUser?.uid))

    val syncState: StateFlow<com.example.data.sync.SyncState> =
        syncManager?.syncState ?: MutableStateFlow(com.example.data.sync.SyncState.Idle).asStateFlow()

    val lastSyncedTimestamp: StateFlow<Long> =
        syncManager?.lastSyncedTimestamp ?: MutableStateFlow(0L).asStateFlow()

    fun syncNow() {
        syncManager?.syncNow()
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Temporary profile model prepared for Phase 2 Firestore synchronization
    private val _preparedProfile = MutableStateFlow<UserProfile?>(null)
    val preparedProfile: StateFlow<UserProfile?> = _preparedProfile.asStateFlow()

    fun clearState() {
        _uiState.value = AuthUiState()
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter your email address.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter a valid email format.")
            return
        }
        if (password.isEmpty()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter your password.")
            return
        }

        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(cleanEmail, password)) {
                is AuthResult.Success -> {
                    val user = result.data
                    _preparedProfile.value = UserProfile(
                        uid = user.uid,
                        displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Sage Scholar",
                        email = user.email.orEmpty(),
                        createdAt = System.currentTimeMillis()
                    )
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isActionSuccessful = true,
                        successMessage = "Welcome back, ${_preparedProfile.value?.displayName}!"
                    )
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun createAccount(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        if (cleanName.isEmpty()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter your full name.")
            return
        }
        if (cleanEmail.isEmpty()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter your email address.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter a valid email address.")
            return
        }
        if (password.isEmpty()) {
            _uiState.value = AuthUiState(errorMessage = "Please create a password.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(errorMessage = "Password must be at least 6 characters.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState(errorMessage = "Passwords do not match. Please re-enter.")
            return
        }

        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = authRepository.createAccount(cleanName, cleanEmail, password)) {
                is AuthResult.Success -> {
                    _preparedProfile.value = result.data
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isActionSuccessful = true,
                        successMessage = "Account created successfully! Welcome to Sage."
                    )
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit) {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter your email to receive a reset link.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter a valid email format.")
            return
        }

        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = authRepository.sendPasswordReset(cleanEmail)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isActionSuccessful = true,
                        successMessage = "Password reset instructions have been sent to $cleanEmail. Please check your inbox."
                    )
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun logout() {
        authRepository.signOut()
        _preparedProfile.value = null
        _uiState.value = AuthUiState(successMessage = "You have been logged out.")
    }

    class Factory(private val application: android.app.Application) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val syncManager = com.example.data.sync.SageSyncManager(application)
            return AuthViewModel(syncManager = syncManager) as T
        }
    }
}
