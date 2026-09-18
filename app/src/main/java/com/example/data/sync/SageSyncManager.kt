package com.example.data.sync

import android.content.Context
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.local.PreferencesManager
import com.example.data.local.SageDatabase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SageSyncManager(
    private val context: Context,
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val preferencesManager: PreferencesManager = PreferencesManager(context),
    database: SageDatabase = SageDatabase.getInstance(context)
) {
    private val dao = database.sageDao()
    private val firestoreSyncService = FirestoreSyncService(
        dao = dao,
        preferencesManager = preferencesManager
    )

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncedTimestamp = MutableStateFlow<Long>(0L)
    val lastSyncedTimestamp: StateFlow<Long> = _lastSyncedTimestamp.asStateFlow()

    init {
        // Observe auth state changes to automatically trigger synchronization on login
        scope.launch {
            authRepository.authStateFlow.collect { user ->
                if (user != null) {
                    performSync(user)
                } else {
                    _syncState.value = SyncState.Idle
                }
            }
        }
    }

    /**
     * Request an immediate manual or periodic synchronization.
     */
    fun syncNow() {
        val user = authRepository.currentUser
        if (user == null) {
            _syncState.value = SyncState.Pending("Authentication required for cloud sync.")
            return
        }
        scope.launch {
            performSync(user)
        }
    }

    private suspend fun performSync(user: FirebaseUser) {
        _syncState.value = SyncState.Syncing
        val result = firestoreSyncService.syncUserData(user)
        when (result) {
            is SyncResult.Success -> {
                val time = System.currentTimeMillis()
                _lastSyncedTimestamp.value = time
                _syncState.value = SyncState.Synced(time)
            }
            is SyncResult.Error -> {
                _syncState.value = SyncState.Error(result.message)
            }
        }
    }
}
