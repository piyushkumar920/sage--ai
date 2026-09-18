package com.example.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.admin.AdminAuditLogEntry
import com.example.data.admin.AdminSupportTicket
import com.example.data.admin.AdminUserDetails
import com.example.data.admin.AdminUserProfileItem
import com.example.data.admin.SystemHealthReport
import com.example.data.auth.AdminAuthManager
import com.example.data.sync.FirestoreRecoverySnapshot
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.screens.DiagnosticScreen
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminConsoleScreen(
    adminViewModel: AdminViewModel,
    backendUrl: String,
    onSaveBackendUrl: (String) -> Unit,
    onResetBackendDefaults: () -> Unit,
    onBack: () -> Unit
) {
    val activeTab by adminViewModel.activeTab.collectAsState()
    val healthReport by adminViewModel.systemHealthReport.collectAsState()
    val isRefreshingOverview by adminViewModel.isRefreshingOverview.collectAsState()

    // Dialog state for snapshot restoration
    val showRestoreDialog by adminViewModel.showRestoreConfirmDialog.collectAsState()
    val targetSnapshot by adminViewModel.targetSnapshotToRestore.collectAsState()
    val isRestoring by adminViewModel.isRestoring.collectAsState()
    val restoreMessage by adminViewModel.restoreMessage.collectAsState()

    // Details modal state
    val selectedUserDetails by adminViewModel.selectedUserDetails.collectAsState()
    val isLoadingUserDetails by adminViewModel.isLoadingUserDetails.collectAsState()

    // Ticket modal state
    val selectedTicket by adminViewModel.selectedTicket.collectAsState()

    // Restore confirmation dialog
    if (showRestoreDialog && targetSnapshot != null) {
        AlertDialog(
            onDismissRequest = { if (!isRestoring) adminViewModel.dismissRestoreConfirmDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SageGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Restore Snapshot Confirmation",
                        color = SageTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Restore this snapshot?",
                        color = SageTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "The user's current cloud data will first be backed up before restoration.",
                        color = SageTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Snapshot ID: ${targetSnapshot?.snapshotId}",
                        color = SageGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Reason: ${targetSnapshot?.reason}",
                        color = SageTextMuted,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                GlassButton(
                    text = if (isRestoring) "Restoring..." else "Backup & Restore",
                    onClick = { adminViewModel.executeRestoreSnapshot() },
                    variant = GlassButtonVariant.Primary,
                    modifier = Modifier.testTag("confirm_backup_restore_button")
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { adminViewModel.dismissRestoreConfirmDialog() },
                    enabled = !isRestoring
                ) {
                    Text("Cancel", color = SageTextSecondary)
                }
            },
            containerColor = SageSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Scaffold(
        containerColor = SageBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SageBackground, Color(0xFF0F0F1E), SageBackground)
                    )
                )
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // HEADER
                AdminHeader(
                    adminName = AdminAuthManager.ADMIN_DISPLAY_NAME,
                    isRefreshing = isRefreshingOverview,
                    onRefresh = { adminViewModel.refreshAll() },
                    onBack = onBack
                )

                // COMPACT RUNTIME STATUS CARDS
                CompactRuntimeStatusRow(
                    report = healthReport,
                    isRefreshing = isRefreshingOverview
                )

                // NAVIGATION TABS
                AdminNavigationTabs(
                    activeTab = activeTab,
                    onSelectTab = { adminViewModel.selectTab(it) }
                )

                // TAB CONTENT
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (activeTab) {
                        AdminConsoleTab.USERS -> {
                            AdminUsersSection(
                                adminViewModel = adminViewModel
                            )
                        }

                        AdminConsoleTab.TICKETS -> {
                            AdminTicketsSection(
                                adminViewModel = adminViewModel
                            )
                        }

                        AdminConsoleTab.RECOVERY -> {
                            AdminRecoverySection(
                                adminViewModel = adminViewModel,
                                restoreMessage = restoreMessage
                            )
                        }

                        AdminConsoleTab.HEALTH -> {
                            AdminHealthSection(
                                adminViewModel = adminViewModel
                            )
                        }

                        AdminConsoleTab.AUDIT -> {
                            AdminAuditSection(
                                adminViewModel = adminViewModel
                            )
                        }

                        AdminConsoleTab.DIAGNOSTICS -> {
                            DiagnosticScreen(
                                isOnline = healthReport?.networkConnected ?: true,
                                isAiConnected = healthReport?.geminiConnected ?: true,
                                lastRequestSuccess = healthReport?.renderBackendConnected ?: false,
                                lastError = healthReport?.lastKnownError ?: "",
                                lastLatencyMs = healthReport?.renderBackendLatencyMs ?: 0L,
                                isTesting = isRefreshingOverview,
                                backendUrl = backendUrl,
                                onRunTest = { adminViewModel.refreshOverview() },
                                onSaveBackendUrl = onSaveBackendUrl,
                                onBack = { adminViewModel.selectTab(AdminConsoleTab.USERS) },
                                onResetDefaults = onResetBackendDefaults
                            )
                        }
                    }

                    // USER DETAILS MODAL OVERLAY
                    if (selectedUserDetails != null) {
                        UserDetailsModal(
                            details = selectedUserDetails!!,
                            onClose = { adminViewModel.closeUserDetails() },
                            onRefresh = { adminViewModel.openUserDetails(selectedUserDetails!!.userProfile.uid) }
                        )
                    }

                    // TICKET DETAILS MODAL OVERLAY
                    if (selectedTicket != null) {
                        TicketDetailsModal(
                            ticket = selectedTicket!!,
                            onClose = { adminViewModel.selectTicket(null) },
                            onUpdate = { newStatus, note ->
                                adminViewModel.updateTicket(
                                    ticketId = selectedTicket!!.ticketId,
                                    newStatus = newStatus,
                                    adminNote = note,
                                    targetUserId = selectedTicket!!.userId
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHeader(
    adminName: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("admin_console_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Settings",
                    tint = SageTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Developer Console",
                        color = SageTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SageGold.copy(alpha = 0.2f))
                            .border(1.dp, SageGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ADMIN",
                            color = SageGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Text(
                    text = "Admin • $adminName",
                    color = SageGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier.testTag("admin_refresh_all_button")
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = SageGold,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Status",
                    tint = SageGold
                )
            }
        }
    }
}

@Composable
private fun CompactRuntimeStatusRow(
    report: SystemHealthReport?,
    isRefreshing: Boolean
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactStatusChip(
            title = "Firebase Auth",
            value = when {
                report == null -> "Checking..."
                report.firebaseAuthConnected -> "Active (Piyush)"
                else -> "Offline"
            },
            isOk = report?.firebaseAuthConnected == true,
            icon = Icons.Default.VerifiedUser
        )

        CompactStatusChip(
            title = "Firestore",
            value = when {
                report == null -> "Checking..."
                report.firestoreConnected -> "Connected"
                else -> "Error"
            },
            isOk = report?.firestoreConnected == true,
            icon = Icons.Default.Cloud
        )

        CompactStatusChip(
            title = "Render Backend",
            value = when {
                report == null -> "Checking..."
                report.renderBackendConnected -> "${report.renderBackendLatencyMs}ms"
                else -> "Error"
            },
            isOk = report?.renderBackendConnected == true,
            icon = Icons.Default.Speed
        )

        CompactStatusChip(
            title = "Gemini API",
            value = when {
                report == null -> "Connecting..."
                report.geminiConnected -> "2.5/3.5 Flash"
                else -> "Unavailable"
            },
            isOk = report?.geminiConnected == true,
            icon = Icons.Default.Assessment
        )

        CompactStatusChip(
            title = "Network",
            value = when {
                report == null -> "Checking..."
                report.networkConnected -> report.networkDetails
                else -> "Offline"
            },
            isOk = report?.networkConnected == true,
            icon = Icons.Default.Wifi
        )

        CompactStatusChip(
            title = "Room SQLite",
            value = when {
                report == null -> "Checking..."
                report.roomDatabaseConnected -> "Active"
                else -> "Error"
            },
            isOk = report?.roomDatabaseConnected == true,
            icon = Icons.Default.Storage
        )

        CompactStatusChip(
            title = "Cloud Sync",
            value = "Engine v2 Active",
            isOk = true,
            icon = Icons.Default.CloudSync
        )
    }
}

@Composable
private fun CompactStatusChip(
    title: String,
    value: String,
    isOk: Boolean,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SageGlassL1)
            .border(
                1.dp,
                if (isOk) SageSuccess.copy(alpha = 0.4f) else SageGold.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isOk) SageSuccess else SageGold,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = title,
                color = SageTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = SageTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminNavigationTabs(
    activeTab: AdminConsoleTab,
    onSelectTab: (AdminConsoleTab) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AdminConsoleTab.values().forEach { tab ->
            val isSelected = activeTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) SageGold.copy(alpha = 0.22f) else SageGlassL1)
                    .border(
                        1.dp,
                        if (isSelected) SageGold else SageGlassBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("admin_tab_${tab.name.lowercase()}")
            ) {
                Text(
                    text = tab.title,
                    color = if (isSelected) SageGold else SageTextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ==========================================
// SECTION A & B: USER MANAGEMENT & PROGRESS
// ==========================================

@Composable
private fun AdminUsersSection(
    adminViewModel: AdminViewModel
) {
    val users by adminViewModel.usersList.collectAsState()
    val searchQuery by adminViewModel.userSearchQuery.collectAsState()
    val isLoading by adminViewModel.isLoadingUsers.collectAsState()
    val error by adminViewModel.usersError.collectAsState()

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) {
            users
        } else {
            val q = searchQuery.trim().lowercase()
            users.filter {
                it.displayName.lowercase().contains(q) ||
                it.email.lowercase().contains(q) ||
                it.uid.lowercase().contains(q) ||
                it.programme.lowercase().contains(q) ||
                it.currentCourse.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { adminViewModel.setUserSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("user_management_search_field"),
            placeholder = { Text("Search by name, email, UID, course...", color = SageTextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = SageGold)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SageGold,
                unfocusedBorderColor = SageGlassBorder,
                focusedTextColor = SageTextPrimary,
                unfocusedTextColor = SageTextPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredUsers.size} Users Registered",
                color = SagePrimaryLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            GlassButton(
                text = "Reload",
                onClick = { adminViewModel.loadUsers() },
                variant = GlassButtonVariant.Secondary,
                modifier = Modifier
                    .height(34.dp)
                    .testTag("reload_users_button")
            )
        }

        if (error != null) {
            Text(
                text = error ?: "",
                color = SageError,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SageGold)
            }
        } else if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "No users found in Firestore." else "No matching users.",
                    color = SageTextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredUsers, key = { it.uid }) { user ->
                    UserCard(
                        user = user,
                        onClick = { adminViewModel.openUserDetails(user.uid) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: AdminUserProfileItem,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.uid.take(8)}"),
        level = GlassLevel.L2,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SageGold.copy(alpha = 0.2f))
                        .border(1.dp, SageGold.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.take(1).uppercase(),
                        color = SageGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName,
                        color = SageTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email.ifEmpty { "No email associated" },
                        color = SageTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SageGlassL3)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Sem ${user.semester}",
                        color = SagePrimaryLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "UID: ${user.uid}",
                color = SageTextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${user.programme} • ${user.currentCourse}",
                    color = SageTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${user.progressPercentage}% Complete",
                    color = SageGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Compact Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SageGlassL1)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (user.progressPercentage / 100f).coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SageGold)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (user.lastSyncTime > 0) "Last Sync: ${dateFormat.format(Date(user.lastSyncTime))}" else "Sync: Active",
                    color = SageTextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "${user.completedTopicsCount} topics • ${user.quizzesCount} quizzes",
                    color = SagePrimaryLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ==========================================
// SECTION C: SUPPORT TICKETS
// ==========================================

@Composable
private fun AdminTicketsSection(
    adminViewModel: AdminViewModel
) {
    val tickets by adminViewModel.ticketsList.collectAsState()
    val filter by adminViewModel.ticketFilter.collectAsState()
    val isLoading by adminViewModel.isLoadingTickets.collectAsState()
    val opMessage by adminViewModel.ticketOperationMessage.collectAsState()

    val filteredTickets = remember(tickets, filter) {
        if (filter == "ALL") {
            tickets
        } else {
            tickets.filter { it.status.equals(filter, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "OPEN", "IN_PROGRESS", "RESOLVED").forEach { f ->
                val isSelected = filter == f
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SageGold.copy(alpha = 0.2f) else SageGlassL1)
                        .border(1.dp, if (isSelected) SageGold else SageGlassBorder, RoundedCornerShape(8.dp))
                        .clickable { adminViewModel.setTicketFilter(f) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = f.replace("_", " "),
                        color = if (isSelected) SageGold else SageTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (opMessage != null) {
            Text(
                text = opMessage ?: "",
                color = SageGold,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SageGold)
            }
        } else if (filteredTickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No support tickets found in this category.",
                    color = SageTextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredTickets, key = { it.ticketId }) { ticket ->
                    SupportTicketCard(
                        ticket = ticket,
                        onClick = { adminViewModel.selectTicket(ticket) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportTicketCard(
    ticket: AdminSupportTicket,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ticket_card_${ticket.ticketId.take(8)}"),
        level = GlassLevel.L2,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SagePrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = ticket.category,
                        color = SagePrimaryLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val (badgeBg, badgeFg) = when (ticket.status) {
                    "OPEN" -> Pair(SageWarning.copy(alpha = 0.2f), SageWarning)
                    "IN_PROGRESS" -> Pair(SageGold.copy(alpha = 0.2f), SageGold)
                    "RESOLVED" -> Pair(SageSuccess.copy(alpha = 0.2f), SageSuccess)
                    else -> Pair(SageGlassL3, SageTextMuted)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = ticket.status.replace("_", " "),
                        color = badgeFg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = ticket.subject,
                color = SageTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = ticket.description,
                color = SageTextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "From: ${ticket.userName}",
                    color = SageTextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = dateFormat.format(Date(ticket.createdAt)),
                    color = SageTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ==========================================
// SECTION D: DATA RECOVERY
// ==========================================

@Composable
private fun AdminRecoverySection(
    adminViewModel: AdminViewModel,
    restoreMessage: String?
) {
    val users by adminViewModel.usersList.collectAsState()
    val selectedUser by adminViewModel.selectedRecoveryUser.collectAsState()
    val snapshots by adminViewModel.userSnapshots.collectAsState()
    val isLoadingSnapshots by adminViewModel.isLoadingSnapshots.collectAsState()
    val isRestoring by adminViewModel.isRestoring.collectAsState()

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "SELECT USER FOR RECOVERY",
            color = SagePrimaryLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
        )

        // User selector horizontal row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users, key = { it.uid }) { user ->
                val isSelected = selectedUser?.uid == user.uid
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) SageGold.copy(alpha = 0.25f) else SageGlassL1)
                        .border(1.dp, if (isSelected) SageGold else SageGlassBorder, RoundedCornerShape(10.dp))
                        .clickable { adminViewModel.selectRecoveryUser(user) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = user.displayName,
                            color = if (isSelected) SageGold else SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "UID: ${user.uid.take(8)}...",
                            color = SageTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (restoreMessage != null) {
            Text(
                text = restoreMessage,
                color = SageGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Text(
            text = "AVAILABLE RECOVERY SNAPSHOTS (MAX 5 PRESERVED)",
            color = SageGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
        )

        if (isLoadingSnapshots) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SageGold)
            }
        } else if (snapshots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recovery snapshots found for selected user.\nSnapshots are generated automatically before sync and restore operations.",
                    color = SageTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(snapshots, key = { it.snapshotId }) { snap ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = SageGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = snap.snapshotId,
                                        color = SageTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SageGlassL3)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = dateFormat.format(Date(snap.createdAt)),
                                        color = SageTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Text(
                                text = "Reason: ${snap.reason}",
                                color = SageTextSecondary,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${snap.progressItemsCount} Progress Topics • ${snap.quizResultsCount} Quiz Results",
                                    color = SagePrimaryLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                GlassButton(
                                    text = "Restore Snapshot",
                                    onClick = { adminViewModel.promptRestoreSnapshot(snap) },
                                    icon = Icons.Default.Restore,
                                    variant = GlassButtonVariant.Secondary,
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("restore_btn_${snap.snapshotId.take(8)}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SECTION E: SYSTEM HEALTH
// ==========================================

@Composable
private fun AdminHealthSection(
    adminViewModel: AdminViewModel
) {
    val report by adminViewModel.systemHealthReport.collectAsState()
    val isChecking by adminViewModel.isRefreshingOverview.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REAL RUNTIME SUBSYSTEM STATUS",
                color = SageGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            GlassButton(
                text = if (isChecking) "Checking..." else "Run Full Health Check",
                onClick = { adminViewModel.runHealthCheck() },
                icon = Icons.Default.Speed,
                variant = GlassButtonVariant.Primary,
                modifier = Modifier
                    .height(36.dp)
                    .testTag("run_full_health_check_button")
            )
        }

        // Subsystems
        HealthSubsystemCard(
            title = "Network Connectivity",
            status = if (report?.networkConnected == true) "CONNECTED" else "OFFLINE",
            isOk = report?.networkConnected == true,
            details = report?.networkDetails ?: "Checking...",
            icon = Icons.Default.Wifi
        )

        HealthSubsystemCard(
            title = "Firebase Authentication",
            status = if (report?.firebaseAuthConnected == true) "AUTHENTICATED" else "GUEST / UNKNOWN",
            isOk = report?.firebaseAuthConnected == true,
            details = "Current UID: ${report?.currentAuthUid ?: "N/A"}\nEmail: ${report?.currentAuthEmail ?: "N/A"}",
            icon = Icons.Default.VerifiedUser
        )

        HealthSubsystemCard(
            title = "Cloud Firestore",
            status = if (report?.firestoreConnected == true) "ONLINE" else "ERROR",
            isOk = report?.firestoreConnected == true,
            details = report?.firestoreDetails ?: "Checking...",
            icon = Icons.Default.Cloud
        )

        HealthSubsystemCard(
            title = "Render Backend & Proxy",
            status = if (report?.renderBackendConnected == true) "PASS" else "FAIL",
            isOk = report?.renderBackendConnected == true,
            details = "URL: ${report?.renderBackendUrl ?: "Render"}\nStatus: ${report?.renderBackendStatus ?: "N/A"}\nLatency: ${report?.renderBackendLatencyMs ?: -1}ms",
            icon = Icons.Default.Build
        )

        HealthSubsystemCard(
            title = "Gemini AI Engine",
            status = if (report?.geminiConnected == true) "READY" else "UNAVAILABLE",
            isOk = report?.geminiConnected == true,
            details = "Model: ${report?.geminiModel ?: "gemini-2.5-flash / gemini-3.5-flash"}\nServer-Side Proxy Architecture Enforced",
            icon = Icons.Default.Assessment
        )

        HealthSubsystemCard(
            title = "Local Room Persistence",
            status = if (report?.roomDatabaseConnected == true) "ACTIVE" else "ERROR",
            isOk = report?.roomDatabaseConnected == true,
            details = report?.roomStats ?: "Room SQLite active",
            icon = Icons.Default.Storage
        )

        HealthSubsystemCard(
            title = "Bidirectional Cloud Sync",
            status = "ACTIVE",
            isOk = true,
            details = "Engine v2 Monotonic Merge • Snapshots Protected",
            icon = Icons.Default.CloudSync
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HealthSubsystemCard(
    title: String,
    status: String,
    isOk: Boolean,
    details: String,
    icon: ImageVector
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        level = GlassLevel.L2,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isOk) SageSuccess else SageGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = SageTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isOk) SageSuccess.copy(alpha = 0.2f) else SageError.copy(alpha = 0.2f))
                        .border(
                            1.dp,
                            if (isOk) SageSuccess.copy(alpha = 0.5f) else SageError.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = status,
                        color = if (isOk) SageSuccess else SageError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = details,
                color = SageTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// ==========================================
// SECTION F: AUDIT LOG
// ==========================================

@Composable
private fun AdminAuditSection(
    adminViewModel: AdminViewModel
) {
    val logs by adminViewModel.auditLogs.collectAsState()
    val filter by adminViewModel.auditActionFilter.collectAsState()
    val isLoading by adminViewModel.isLoadingAudit.collectAsState()

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    val filteredLogs = remember(logs, filter) {
        if (filter == "ALL") {
            logs
        } else {
            logs.filter { it.action.equals(filter, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "VIEW_USER_DETAILS", "UPDATE_SUPPORT_TICKET", "RESTORE_SNAPSHOT", "SYSTEM_HEALTH_CHECK").forEach { f ->
                val isSelected = filter == f
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SageGold.copy(alpha = 0.2f) else SageGlassL1)
                        .border(1.dp, if (isSelected) SageGold else SageGlassBorder, RoundedCornerShape(8.dp))
                        .clickable { adminViewModel.setAuditActionFilter(f) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = f.replace("_", " "),
                        color = if (isSelected) SageGold else SageTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SageGold)
            }
        } else if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No administrative audit log events recorded yet.",
                    color = SageTextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredLogs, key = { it.logId }) { log ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.action.replace("_", " "),
                                    color = SageGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    color = SageTextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Text(
                                text = "Admin: ${log.adminName} (${log.adminUid.take(8)}...)",
                                color = SageTextSecondary,
                                fontSize = 11.sp
                            )

                            if (log.targetUserId.isNotBlank()) {
                                Text(
                                    text = "Target User UID: ${log.targetUserId}",
                                    color = SagePrimaryLight,
                                    fontSize = 10.sp
                                )
                            }

                            if (log.targetTicketId.isNotBlank()) {
                                Text(
                                    text = "Target Ticket: ${log.targetTicketId}",
                                    color = SageTextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            if (log.targetSnapshotId.isNotBlank()) {
                                Text(
                                    text = "Target Snapshot: ${log.targetSnapshotId}",
                                    color = SageTextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            if (log.metadata.isNotEmpty()) {
                                Text(
                                    text = "Metadata: ${log.metadata}",
                                    color = SageTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// MODALS: USER DETAILS & TICKET DETAILS
// ==========================================

@Composable
private fun UserDetailsModal(
    details: AdminUserDetails,
    onClose: () -> Unit,
    onRefresh: () -> Unit
) {
    val profile = details.userProfile
    val academic = details.academicProfile
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .clickable(enabled = false) {}
                .padding(vertical = 16.dp),
            level = GlassLevel.L3,
            shape = RoundedCornerShape(20.dp),
            borderColor = SageGold.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = profile.displayName,
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = profile.email.ifEmpty { "No email" },
                            color = SageTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = SageGold)
                        }
                        TextButton(onClick = onClose) {
                            Text("Close", color = SageGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = SageGlassBorder, thickness = 1.dp)

                Text(text = "USER PROFILE", color = SageGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                DetailRow(label = "Firebase UID", value = profile.uid)
                DetailRow(label = "Created Date", value = dateFormat.format(Date(profile.createdAt)))
                DetailRow(label = "Academic Programme", value = academic?.programme ?: "B. Tech CSE (AI & ML)")
                DetailRow(label = "Regulation", value = academic?.regulation ?: "R25")
                DetailRow(label = "Current Semester", value = "Semester ${academic?.currentSemester ?: 1}")
                DetailRow(label = "Active Roadmap", value = academic?.activeRoadmapTitle ?: "CS101")
                DetailRow(label = "Current Topic", value = academic?.currentTopicTitle?.ifEmpty { "Not Started" } ?: "N/A")

                HorizontalDivider(color = SageGlassBorder, thickness = 1.dp)

                Text(text = "PROGRESS & QUIZZES", color = SageGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                DetailRow(label = "Completed Topics", value = "${details.progressList.count { it.status == "COMPLETED" }} topics")
                DetailRow(label = "In-Progress Topics", value = "${details.progressList.count { it.status == "IN_PROGRESS" }} topics")
                DetailRow(label = "Quizzes Attempted", value = "${details.quizResults.size} quizzes")
                val avgScore = if (details.quizResults.isNotEmpty()) {
                    val sum = details.quizResults.sumOf { if (it.totalQuestions > 0) (it.score * 100) / it.totalQuestions else 0 }
                    sum / details.quizResults.size
                } else 0
                DetailRow(label = "Average Quiz Score", value = "$avgScore%")

                HorizontalDivider(color = SageGlassBorder, thickness = 1.dp)

                Text(text = "CLOUD SYNCHRONIZATION", color = SageGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                val lastSync = details.syncMetadata?.lastSyncTimestamp ?: profile.updatedAt
                DetailRow(label = "Last Cloud Sync", value = dateFormat.format(Date(lastSync)))
                DetailRow(label = "Synced Topics", value = "${details.syncMetadata?.totalSyncedTopics ?: details.progressList.size}")
                DetailRow(label = "Available Snapshots", value = "${details.snapshots.size} recovery snapshots")

                Spacer(modifier = Modifier.height(6.dp))
                GlassButton(
                    text = "Close Profile",
                    onClick = onClose,
                    variant = GlassButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TicketDetailsModal(
    ticket: AdminSupportTicket,
    onClose: () -> Unit,
    onUpdate: (newStatus: String, adminNote: String) -> Unit
) {
    var adminNote by remember(ticket) { mutableStateOf(ticket.adminNote) }
    var selectedStatus by remember(ticket) { mutableStateOf(ticket.status) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .clickable(enabled = false) {}
                .padding(vertical = 16.dp),
            level = GlassLevel.L3,
            shape = RoundedCornerShape(20.dp),
            borderColor = SageGold.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ticket: ${ticket.ticketId.take(12)}",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "From: ${ticket.userName} (${ticket.userEmail})",
                            color = SageTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    TextButton(onClick = onClose) {
                        Text("Close", color = SageGold, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = SageGlassBorder, thickness = 1.dp)

                DetailRow(label = "Category", value = ticket.category)
                DetailRow(label = "Created", value = dateFormat.format(Date(ticket.createdAt)))
                DetailRow(label = "User UID", value = ticket.userId)

                Text(
                    text = "Subject: ${ticket.subject}",
                    color = SageTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    text = ticket.description,
                    color = SageTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                if (ticket.diagnostics.isNotEmpty()) {
                    Text(
                        text = "SAFE DIAGNOSTICS ATTACHED",
                        color = SagePrimaryLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    ticket.diagnostics.forEach { (k, v) ->
                        DetailRow(label = k, value = v)
                    }
                }

                HorizontalDivider(color = SageGlassBorder, thickness = 1.dp)

                Text(text = "ADMIN ACTION & STATUS", color = SageGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                // Status selection buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED").forEach { st ->
                        val isSel = selectedStatus == st
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) SageGold.copy(alpha = 0.25f) else SageGlassL1)
                                .border(1.dp, if (isSel) SageGold else SageGlassBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedStatus = st }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = st.replace("_", " "),
                                color = if (isSel) SageGold else SageTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = adminNote,
                    onValueChange = { adminNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add administrative resolution notes...", color = SageTextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageGold,
                        unfocusedBorderColor = SageGlassBorder,
                        focusedTextColor = SageTextPrimary,
                        unfocusedTextColor = SageTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        text = "Mark Resolved",
                        onClick = {
                            selectedStatus = "RESOLVED"
                            onUpdate("RESOLVED", adminNote.ifEmpty { "Resolved by Admin Piyush Kumar." })
                        },
                        variant = GlassButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )

                    GlassButton(
                        text = "Save Updates",
                        onClick = {
                            onUpdate(selectedStatus, adminNote)
                        },
                        variant = GlassButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = SageTextSecondary, fontSize = 11.sp)
        Text(
            text = value,
            color = SageTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
