package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.auth.AuthViewModel
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.support.ReportProblemDialog
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    academicProfile: com.example.data.profile.AcademicProfile? = null,
    onOpenAcademicProfileDialog: () -> Unit = {},
    onOpenAuth: (AuthScreenMode) -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenDeveloperConsole: () -> Unit = onOpenDiagnostics,
    backendStatus: String = "Online",
    syncStatus: String = "Active",
    lastErrorCode: String? = null,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoggedIn = currentUser != null
    val isAdmin = com.example.data.auth.AdminAuthManager.isUserAdmin(currentUser?.uid)

    var showManageAccountModal by remember { mutableStateOf(false) }
    var showReportProblemModal by remember { mutableStateOf(false) }

    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Settings",
                                color = SageTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // SECTION 1: ACCOUNT
                Text(
                    text = "ACCOUNT",
                    color = SagePrimaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                if (!isLoggedIn) {
                    // Logged-out state: Clean Glass Card to Login / Create Account
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_guest_card"),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(20.dp),
                        onClick = { onOpenAuth(AuthScreenMode.LOGIN) }
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(SageGlassL1)
                                    .border(1.dp, SageGlassBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = SagePrimaryLight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Login / Create Account",
                                    color = SageTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Sign in to connect cloud account and preserve progress",
                                    color = SageTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // Logged-in state
                    val user = currentUser
                    val displayName = user?.displayName?.ifEmpty { "Sage Scholar" }
                        ?: user?.email?.substringBefore("@")
                        ?: "Sage Scholar"
                    val email = user?.email.orEmpty()

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_connected_card"),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SageGlassL1)
                                        .border(1.dp, SageGlassBorderGlow, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayName.take(1).uppercase(),
                                        color = SagePrimaryLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayName,
                                        color = SageTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = email,
                                        color = SageTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Cloud account connected badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SageSuccess.copy(alpha = 0.12f))
                                    .border(1.dp, SageSuccess.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SageSuccess)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cloud account connected",
                                    color = SageSuccess,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }

                            // Cloud Sync Status & Sync Now Action
                            val syncState by authViewModel.syncState.collectAsState()
                            val lastSyncTime by authViewModel.lastSyncedTimestamp.collectAsState()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SageGlassL1)
                                    .border(1.dp, SageGlassBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val (statusDotColor, statusText) = when (syncState) {
                                            is com.example.data.sync.SyncState.Syncing -> SageGold to "⟳ Syncing..."
                                            is com.example.data.sync.SyncState.Synced -> SageSuccess to "● Synced"
                                            is com.example.data.sync.SyncState.Pending -> SageGold to "⚠ Sync pending"
                                            is com.example.data.sync.SyncState.Error -> SageError to "⚠ Sync error"
                                            is com.example.data.sync.SyncState.Idle -> SageSuccess to "● Synced"
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(statusDotColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = statusText,
                                            color = statusDotColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    val formattedTime = if (lastSyncTime > 0L) {
                                        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                                        "Last synchronized: ${sdf.format(java.util.Date(lastSyncTime))}"
                                    } else {
                                        "Last synchronized: Just now"
                                    }

                                    Text(
                                        text = formattedTime,
                                        color = SageTextSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 13.dp, top = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                GlassButton(
                                    text = "Sync Now",
                                    onClick = { authViewModel.syncNow() },
                                    icon = Icons.Default.Refresh,
                                    variant = GlassButtonVariant.Secondary,
                                    modifier = Modifier
                                        .testTag("sync_now_button")
                                )
                            }

                            HorizontalDivider(
                                color = SageGlassBorder,
                                thickness = 1.dp
                            )

                            // Action buttons: [Manage Account] and [Logout]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlassButton(
                                    text = "Manage Account",
                                    onClick = { showManageAccountModal = true },
                                    icon = Icons.Default.ManageAccounts,
                                    variant = GlassButtonVariant.Secondary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("manage_account_button")
                                )

                                GlassButton(
                                    text = "Logout",
                                    onClick = {
                                        authViewModel.logout()
                                    },
                                    icon = Icons.Default.Logout,
                                    variant = GlassButtonVariant.Outline,
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .testTag("logout_button")
                                )
                            }
                        }
                    }
                }

                // Manage Account Details Dialog/Card
                if (showManageAccountModal && isLoggedIn) {
                    val user = currentUser
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manage_account_details_card"),
                        level = GlassLevel.L3,
                        borderColor = SagePrimaryLight.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Account Details",
                                    color = SageTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Close",
                                    color = SageGold,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { showManageAccountModal = false }
                                        .padding(4.dp)
                                )
                            }

                            HorizontalDivider(color = SageGlassBorder, thickness = 1.dp)

                            AccountDetailRow(label = "User ID", value = user?.uid?.take(16)?.plus("...") ?: "N/A")
                            AccountDetailRow(label = "Email Verified", value = if (user?.isEmailVerified == true) "Yes" else "No")
                            AccountDetailRow(label = "Provider", value = "Firebase Email/Password")

                            Spacer(modifier = Modifier.height(4.dp))
                            GlassButton(
                                text = "Send Password Reset Email",
                                onClick = {
                                    val email = user?.email
                                    if (!email.isNullOrBlank()) {
                                        authViewModel.sendPasswordReset(email) {}
                                    }
                                },
                                icon = Icons.Default.Lock,
                                variant = GlassButtonVariant.Secondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("manage_account_reset_password_button")
                            )
                        }
                    }
                }

                // SECTION 2: ACADEMIC PROFILE (Phase C2.5)
                Text(
                    text = "ACADEMIC PROFILE",
                    color = SagePrimaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_academic_profile_card"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SagePrimary.copy(alpha = 0.2f))
                                        .border(1.dp, SagePrimaryLight.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🎓", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = academicProfile?.departmentName ?: "No Profile Selected",
                                        color = SageTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (academicProfile != null) "Semester ${academicProfile.semester} • ${academicProfile.regulation}" else "Set up to personalize your curriculum",
                                        color = SageTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (academicProfile != null) {
                            HorizontalDivider(color = SageGlassBorder, thickness = 1.dp)

                            AccountDetailRow(label = "Programme", value = academicProfile.programmeName)
                            AccountDetailRow(label = "Department ID", value = academicProfile.departmentId.uppercase())
                            AccountDetailRow(label = "Regulation", value = academicProfile.regulation)
                            AccountDetailRow(label = "Current Semester", value = "Semester ${academicProfile.semester}")
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        GlassButton(
                            text = if (academicProfile == null) "Set Up Academic Profile" else "Change Academic Profile",
                            onClick = onOpenAcademicProfileDialog,
                            icon = Icons.Default.Build,
                            variant = GlassButtonVariant.Primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("change_academic_profile_button")
                        )
                    }
                }

                // SECTION 3: LEARNING PREFERENCES
                Text(
                    text = "PREFERENCES",
                    color = SagePrimaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PreferenceRow(
                            icon = Icons.Default.CloudDone,
                            title = "Local Learning Persistence",
                            subtitle = "Room SQLite database active. Guest and account progress are preserved safely on-device."
                        )
                        HorizontalDivider(color = SageGlassBorder, thickness = 1.dp)
                        PreferenceRow(
                            icon = Icons.Default.Security,
                            title = "Server-Side AI Privacy",
                            subtitle = "Gemini interactions route via Render backend proxy. No credentials stored on device."
                        )
                    }
                }

                // SECTION 3: SUPPORT & FEEDBACK (Available to all users)
                Text(
                    text = "SUPPORT & FEEDBACK",
                    color = SagePrimaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_report_problem_card"),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(18.dp),
                    onClick = { showReportProblemModal = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SagePrimaryLight.copy(alpha = 0.15f))
                                .border(1.dp, SagePrimaryLight.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Report a Problem",
                                color = SageTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Submit a support ticket with diagnostic details to administration",
                                color = SageTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SagePrimaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // SECTION 4: DEVELOPER & DIAGNOSTICS
                // STRICT SECURITY REQUIREMENT:
                // Only rendered for Admin UID: SedsiyiYU7Pn2B8nQ3f7PqJsMTM2 (Piyush Kumar)
                // For all other authenticated users and guest users: completely hidden, no empty card.
                if (isAdmin) {
                    Text(
                        text = "DEVELOPER & SYSTEM CONTROL",
                        color = SageGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_diagnostics_card"),
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(18.dp),
                        onClick = onOpenDeveloperConsole
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SageGold.copy(alpha = 0.15f))
                                    .border(1.dp, SageGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = SageGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Developer Console",
                                        color = SageTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SageGold.copy(alpha = 0.2f))
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
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Diagnostics, user inspection, health, recovery & audit log",
                                    color = SageTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SageGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // App Credit / About
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "This app is built by Piyush Kumar",
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "CSE (AI/ML)",
                        color = SageTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (showReportProblemModal) {
            ReportProblemDialog(
                onDismiss = { showReportProblemModal = false },
                onOpenLogin = { onOpenAuth(AuthScreenMode.LOGIN) },
                backendStatus = backendStatus,
                syncStatus = syncStatus,
                lastErrorCode = lastErrorCode
            )
        }
    }
}

@Composable
private fun AccountDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = SageTextSecondary, fontSize = 12.sp)
        Text(text = value, color = SageTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
private fun PreferenceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SagePrimaryLight,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = SageTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = SageTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
