package com.example.data.sync

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Synced(val lastSyncTime: Long) : SyncState()
    data class Pending(val reason: String? = null) : SyncState()
    data class Error(val message: String, val lastAttemptTime: Long = System.currentTimeMillis()) : SyncState()
}

data class SyncConflictReport(
    val entityType: String,
    val entityKey: String,
    val localUpdatedAt: Long,
    val cloudUpdatedAt: Long,
    val resolution: String,
    val resolvedAt: Long = System.currentTimeMillis()
)
