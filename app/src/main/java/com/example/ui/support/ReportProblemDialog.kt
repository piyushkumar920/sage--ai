package com.example.ui.support

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.support.SupportTicketService
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ReportProblemDialog(
    onDismiss: () -> Unit,
    onOpenLogin: () -> Unit,
    backendStatus: String = "Online",
    syncStatus: String = "Active",
    lastErrorCode: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser
    val ticketService = remember { SupportTicketService(context) }

    val categories = remember {
        listOf(
            "Login / Account",
            "AI not responding",
            "AI answer problem",
            "Progress disappeared",
            "Sync problem",
            "Quiz problem",
            "Roadmap problem",
            "Course / Curriculum",
            "App crash",
            "Other"
        )
    }

    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachDiagnostics by remember { mutableStateOf(true) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccessId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable { if (!isSubmitting) onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
                .clickable(enabled = false) {}
                .padding(vertical = 16.dp)
                .testTag("report_problem_dialog_card"),
            level = GlassLevel.L3,
            shape = RoundedCornerShape(20.dp),
            borderColor = SageGlassBorderGlow
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = SageGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Report a Problem",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting
                    ) {
                        Text("Cancel", color = SageTextSecondary, fontSize = 13.sp)
                    }
                }

                HorizontalDivider(color = SageGlassBorder, thickness = 1.dp)

                if (submitSuccessId != null) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SageSuccess,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Problem Report Submitted!",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Ticket ID: $submitSuccessId\nOur administrator Piyush Kumar will review and resolve this issue.",
                            color = SageTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        GlassButton(
                            text = "Close",
                            onClick = onDismiss,
                            variant = GlassButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (currentUser == null) {
                    // Unauthenticated warning
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SageGold.copy(alpha = 0.15f))
                            .border(1.dp, SageGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = SageGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Account Connection Required",
                                    color = SageGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "Please sign in or create an account to submit support tickets so our team can follow up with you on your resolution.",
                                color = SageTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            GlassButton(
                                text = "Sign In / Register",
                                onClick = {
                                    onDismiss()
                                    onOpenLogin()
                                },
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Category Selection
                    Text(
                        text = "PROBLEM CATEGORY",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SageGold.copy(alpha = 0.25f) else SageGlassL1)
                                    .border(1.dp, if (isSelected) SageGold else SageGlassBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) SageGold else SageTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Subject field
                    Text(
                        text = "SUBJECT",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_problem_subject_field"),
                        placeholder = { Text("Brief summary of the problem...", color = SageTextMuted, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGold,
                            unfocusedBorderColor = SageGlassBorder,
                            focusedTextColor = SageTextPrimary,
                            unfocusedTextColor = SageTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Description field
                    Text(
                        text = "DESCRIPTION",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("report_problem_description_field"),
                        placeholder = { Text("Please describe what happened, expected behavior, or steps to reproduce...", color = SageTextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGold,
                            unfocusedBorderColor = SageGlassBorder,
                            focusedTextColor = SageTextPrimary,
                            unfocusedTextColor = SageTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )

                    // Safe diagnostics toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SageGlassL1)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Attach Safe Diagnostics",
                                color = SageTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Includes app version, Android version, and sync status. Never includes passwords or private keys.",
                                color = SageTextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = attachDiagnostics,
                            onCheckedChange = { attachDiagnostics = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SageGold,
                                checkedTrackColor = SageGold.copy(alpha = 0.4f)
                            )
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = SageError,
                            fontSize = 12.sp
                        )
                    }

                    // Submit button
                    GlassButton(
                        text = if (isSubmitting) "Submitting Ticket..." else "Submit Problem Report",
                        onClick = {
                            if (subject.isBlank()) {
                                errorMessage = "Please enter a subject."
                                return@GlassButton
                            }
                            if (description.isBlank()) {
                                errorMessage = "Please enter a description."
                                return@GlassButton
                            }
                            errorMessage = null
                            isSubmitting = true
                            coroutineScope.launch {
                                val result = ticketService.submitTicket(
                                    category = selectedCategory,
                                    subject = subject,
                                    description = description,
                                    includeDiagnostics = attachDiagnostics,
                                    backendStatus = backendStatus,
                                    syncStatus = syncStatus,
                                    lastErrorCode = lastErrorCode
                                )
                                result.onSuccess { ticketId ->
                                    isSubmitting = false
                                    submitSuccessId = ticketId
                                }.onFailure { error ->
                                    isSubmitting = false
                                    errorMessage = error.localizedMessage ?: "Failed to submit ticket."
                                }
                            }
                        },
                        variant = GlassButtonVariant.Primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("submit_problem_report_button")
                    )
                }
            }
        }
    }
}
