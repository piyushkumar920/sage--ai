package com.example.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.admin.AdminAuditLogEntry
import com.example.data.admin.AdminRepository
import com.example.data.admin.AdminSupportTicket
import com.example.data.admin.AdminUserDetails
import com.example.data.admin.AdminUserProfileItem
import com.example.data.admin.SystemHealthReport
import com.example.data.auth.AdminAuthManager
import com.example.data.sync.FirestoreRecoverySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AdminConsoleTab(val title: String) {
    USERS("Users"),
    TICKETS("Tickets"),
    RECOVERY("Recovery"),
    HEALTH("Health"),
    AUDIT("Audit Log"),
    DIAGNOSTICS("Diagnostics")
}

class AdminViewModel(
    application: Application,
    private val adminRepository: AdminRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        AdminRepository(application.applicationContext)
    )

    class Factory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(application) as T
        }
    }

    val isAdmin: Boolean = AdminAuthManager.isAdmin()

    private val _activeTab = MutableStateFlow(AdminConsoleTab.USERS)
    val activeTab: StateFlow<AdminConsoleTab> = _activeTab.asStateFlow()

    // Compact Status Overview Cards
    private val _systemHealthReport = MutableStateFlow<SystemHealthReport?>(null)
    val systemHealthReport: StateFlow<SystemHealthReport?> = _systemHealthReport.asStateFlow()

    private val _isRefreshingOverview = MutableStateFlow(false)
    val isRefreshingOverview: StateFlow<Boolean> = _isRefreshingOverview.asStateFlow()

    // --- Users ---
    private val _usersList = MutableStateFlow<List<AdminUserProfileItem>>(emptyList())
    val usersList: StateFlow<List<AdminUserProfileItem>> = _usersList.asStateFlow()

    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    private val _isLoadingUsers = MutableStateFlow(false)
    val isLoadingUsers: StateFlow<Boolean> = _isLoadingUsers.asStateFlow()

    private val _usersError = MutableStateFlow<String?>(null)
    val usersError: StateFlow<String?> = _usersError.asStateFlow()

    private val _selectedUserDetails = MutableStateFlow<AdminUserDetails?>(null)
    val selectedUserDetails: StateFlow<AdminUserDetails?> = _selectedUserDetails.asStateFlow()

    private val _isLoadingUserDetails = MutableStateFlow(false)
    val isLoadingUserDetails: StateFlow<Boolean> = _isLoadingUserDetails.asStateFlow()

    // --- Support Tickets ---
    private val _ticketsList = MutableStateFlow<List<AdminSupportTicket>>(emptyList())
    val ticketsList: StateFlow<List<AdminSupportTicket>> = _ticketsList.asStateFlow()

    private val _ticketFilter = MutableStateFlow("ALL")
    val ticketFilter: StateFlow<String> = _ticketFilter.asStateFlow()

    private val _isLoadingTickets = MutableStateFlow(false)
    val isLoadingTickets: StateFlow<Boolean> = _isLoadingTickets.asStateFlow()

    private val _selectedTicket = MutableStateFlow<AdminSupportTicket?>(null)
    val selectedTicket: StateFlow<AdminSupportTicket?> = _selectedTicket.asStateFlow()

    private val _ticketOperationMessage = MutableStateFlow<String?>(null)
    val ticketOperationMessage: StateFlow<String?> = _ticketOperationMessage.asStateFlow()

    // --- Data Recovery ---
    private val _selectedRecoveryUser = MutableStateFlow<AdminUserProfileItem?>(null)
    val selectedRecoveryUser: StateFlow<AdminUserProfileItem?> = _selectedRecoveryUser.asStateFlow()

    private val _userSnapshots = MutableStateFlow<List<FirestoreRecoverySnapshot>>(emptyList())
    val userSnapshots: StateFlow<List<FirestoreRecoverySnapshot>> = _userSnapshots.asStateFlow()

    private val _isLoadingSnapshots = MutableStateFlow(false)
    val isLoadingSnapshots: StateFlow<Boolean> = _isLoadingSnapshots.asStateFlow()

    private val _targetSnapshotToRestore = MutableStateFlow<FirestoreRecoverySnapshot?>(null)
    val targetSnapshotToRestore: StateFlow<FirestoreRecoverySnapshot?> = _targetSnapshotToRestore.asStateFlow()

    private val _showRestoreConfirmDialog = MutableStateFlow(false)
    val showRestoreConfirmDialog: StateFlow<Boolean> = _showRestoreConfirmDialog.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _restoreMessage = MutableStateFlow<String?>(null)
    val restoreMessage: StateFlow<String?> = _restoreMessage.asStateFlow()

    // --- Audit Log ---
    private val _auditLogs = MutableStateFlow<List<AdminAuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLogEntry>> = _auditLogs.asStateFlow()

    private val _isLoadingAudit = MutableStateFlow(false)
    val isLoadingAudit: StateFlow<Boolean> = _isLoadingAudit.asStateFlow()

    private val _auditActionFilter = MutableStateFlow("ALL")
    val auditActionFilter: StateFlow<String> = _auditActionFilter.asStateFlow()

    init {
        if (isAdmin) {
            refreshAll()
        }
    }

    fun selectTab(tab: AdminConsoleTab) {
        _activeTab.value = tab
        when (tab) {
            AdminConsoleTab.USERS -> if (_usersList.value.isEmpty()) loadUsers()
            AdminConsoleTab.TICKETS -> loadTickets()
            AdminConsoleTab.RECOVERY -> {
                if (_usersList.value.isEmpty()) loadUsers()
                if (_selectedRecoveryUser.value == null && _usersList.value.isNotEmpty()) {
                    selectRecoveryUser(_usersList.value.first())
                }
            }
            AdminConsoleTab.HEALTH -> runHealthCheck()
            AdminConsoleTab.AUDIT -> loadAuditLogs()
            AdminConsoleTab.DIAGNOSTICS -> {}
        }
    }

    fun refreshAll() {
        refreshOverview()
        loadUsers()
        loadTickets()
        loadAuditLogs()
    }

    fun refreshOverview() {
        viewModelScope.launch {
            _isRefreshingOverview.value = true
            val report = adminRepository.runFullSystemHealthCheck()
            _systemHealthReport.value = report
            _isRefreshingOverview.value = false
        }
    }

    // --- User Management Methods ---

    fun setUserSearchQuery(query: String) {
        _userSearchQuery.value = query
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoadingUsers.value = true
            _usersError.value = null
            val result = adminRepository.getUsersList()
            result.onSuccess { users ->
                _usersList.value = users
                if (_selectedRecoveryUser.value == null && users.isNotEmpty()) {
                    selectRecoveryUser(users.first())
                }
            }.onFailure { error ->
                _usersError.value = error.localizedMessage ?: "Failed to load users."
            }
            _isLoadingUsers.value = false
        }
    }

    fun openUserDetails(uid: String) {
        viewModelScope.launch {
            _isLoadingUserDetails.value = true
            val result = adminRepository.getUserDetails(uid)
            result.onSuccess { details ->
                _selectedUserDetails.value = details
            }.onFailure { error ->
                _usersError.value = "Failed to load user details: ${error.localizedMessage}"
            }
            _isLoadingUserDetails.value = false
        }
    }

    fun closeUserDetails() {
        _selectedUserDetails.value = null
    }

    // --- Support Ticket Methods ---

    fun setTicketFilter(filter: String) {
        _ticketFilter.value = filter
    }

    fun loadTickets() {
        viewModelScope.launch {
            _isLoadingTickets.value = true
            val result = adminRepository.getSupportTickets()
            result.onSuccess { list ->
                _ticketsList.value = list
            }.onFailure { error ->
                _ticketOperationMessage.value = "Error loading tickets: ${error.localizedMessage}"
            }
            _isLoadingTickets.value = false
        }
    }

    fun selectTicket(ticket: AdminSupportTicket?) {
        _selectedTicket.value = ticket
    }

    fun updateTicket(ticketId: String, newStatus: String, adminNote: String, targetUserId: String) {
        viewModelScope.launch {
            _ticketOperationMessage.value = "Updating ticket..."
            val result = adminRepository.updateSupportTicket(ticketId, newStatus, adminNote, targetUserId)
            result.onSuccess {
                _ticketOperationMessage.value = "Ticket updated to $newStatus"
                loadTickets()
                // Update currently open ticket if selected
                _selectedTicket.value = _selectedTicket.value?.copy(
                    status = newStatus,
                    adminNote = adminNote
                )
            }.onFailure { error ->
                _ticketOperationMessage.value = "Update failed: ${error.localizedMessage}"
            }
        }
    }

    // --- Data Recovery Methods ---

    fun selectRecoveryUser(user: AdminUserProfileItem) {
        _selectedRecoveryUser.value = user
        loadUserSnapshots(user.uid)
    }

    private fun loadUserSnapshots(uid: String) {
        viewModelScope.launch {
            _isLoadingSnapshots.value = true
            val result = adminRepository.getUserSnapshots(uid)
            result.onSuccess { list ->
                _userSnapshots.value = list
            }.onFailure {
                _userSnapshots.value = emptyList()
            }
            _isLoadingSnapshots.value = false
        }
    }

    fun promptRestoreSnapshot(snapshot: FirestoreRecoverySnapshot) {
        _targetSnapshotToRestore.value = snapshot
        _showRestoreConfirmDialog.value = true
    }

    fun dismissRestoreConfirmDialog() {
        _showRestoreConfirmDialog.value = false
        _targetSnapshotToRestore.value = null
    }

    fun executeRestoreSnapshot() {
        val user = _selectedRecoveryUser.value ?: return
        val snapshot = _targetSnapshotToRestore.value ?: return
        _showRestoreConfirmDialog.value = false

        viewModelScope.launch {
            _isRestoring.value = true
            _restoreMessage.value = "Creating backup snapshot and applying restore..."
            val result = adminRepository.restoreUserSnapshot(user.uid, snapshot)
            result.onSuccess { backupId ->
                _restoreMessage.value = "Successfully restored! Safety backup: $backupId"
                loadUserSnapshots(user.uid)
                loadUsers()
            }.onFailure { error ->
                _restoreMessage.value = "Restore failed: ${error.localizedMessage}"
            }
            _isRestoring.value = false
        }
    }

    // --- System Health Methods ---

    fun runHealthCheck() {
        viewModelScope.launch {
            _isRefreshingOverview.value = true
            val report = adminRepository.runFullSystemHealthCheck()
            _systemHealthReport.value = report
            _isRefreshingOverview.value = false
        }
    }

    // --- Audit Log Methods ---

    fun setAuditActionFilter(filter: String) {
        _auditActionFilter.value = filter
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            _isLoadingAudit.value = true
            val result = adminRepository.getAuditLogs()
            result.onSuccess { list ->
                _auditLogs.value = list
            }
            _isLoadingAudit.value = false
        }
    }
}
