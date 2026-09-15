package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiConfig
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    isOnline: Boolean,
    isAiConnected: Boolean,
    lastRequestSuccess: Boolean,
    lastError: String,
    lastLatencyMs: Long,
    isTesting: Boolean,
    backendUrl: String,
    onRunTest: () -> Unit,
    onSaveBackendUrl: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    onResetDefaults: (() -> Unit)? = null
) {
    var urlInput by remember { mutableStateOf("") }
    var urlSavedNotification by remember { mutableStateOf<String?>(null) }
    var urlValidationError by remember { mutableStateOf<String?>(null) }

    val formattedLastError = when {
        lastError.contains("HTML", ignoreCase = true) || lastError.contains("HTML_RESPONSE", ignoreCase = true) ->
            "API returned HTML instead of JSON — production routing/deployment problem."
        lastError.isBlank() || lastError.equals("None", ignoreCase = true) ->
            "None"
        else -> lastError
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = SageGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Developer Diagnostics",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SageTextPrimary
                            )
                        }
                    }
                },
                actions = {
                    if (onResetDefaults != null) {
                        OutlinedButton(
                            onClick = onResetDefaults,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("reset_defaults_top_button")
                        ) {
                            Text("Reset", color = SageGold, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SageBackground)
            )
        },
        containerColor = SageBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SageCardBorder, RoundedCornerShape(16.dp))
                    .testTag("diagnostic_status_card"),
                colors = CardDefaults.cardColors(containerColor = SageSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Production Backend Status",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isAiConnected) SageSuccess.copy(alpha = 0.15f) else SageError.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAiConnected) "ONLINE" else "DISCONNECTED",
                                color = if (isAiConnected) SageSuccess else SageError,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    DiagnosticRow(
                        label = "Network Connectivity",
                        value = if (isOnline) "Connected (Internet OK)" else "Offline",
                        isSuccess = isOnline
                    )

                    DiagnosticRow(
                        label = "Backend Service",
                        value = "Cloud Run Node.js API",
                        isSuccess = isAiConnected
                    )

                    DiagnosticRow(
                        label = "Architecture",
                        value = "HTTPS -> Cloud Run -> Node.js -> Gemini 3.5 Flash",
                        isNeutral = true
                    )

                    DiagnosticRow(
                        label = "Endpoint Route",
                        value = "POST /api/chat",
                        isNeutral = true
                    )

                    DiagnosticRow(
                        label = "Expected Content-Type",
                        value = "application/json",
                        isNeutral = true
                    )

                    DiagnosticRow(
                        label = "AI Model",
                        value = GeminiConfig.GEMINI_MODEL,
                        isNeutral = true
                    )

                    DiagnosticRow(
                        label = "Last request",
                        value = if (lastRequestSuccess) "SUCCESS" else "FAILED",
                        isSuccess = lastRequestSuccess
                    )

                    DiagnosticRow(
                        label = "Last latency",
                        value = if (lastLatencyMs > 0) "${lastLatencyMs}ms" else "N/A",
                        isNeutral = true
                    )

                    DiagnosticRow(
                        label = "Last error",
                        value = formattedLastError,
                        isNeutral = formattedLastError == "None",
                        isSuccess = formattedLastError == "None"
                    )
                }
            }

            // Test AI Connection Button
            Button(
                onClick = onRunTest,
                enabled = !isTesting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SagePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("run_connection_test_button")
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Verifying Backend & Gemini AI...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Connection Test", fontWeight = FontWeight.Bold)
                }
            }

            // Production Architecture & Hardening Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SageCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SageRaisedSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SageGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hardened Architecture Guarantees",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "1. Server-Side Secret Isolation: Gemini credentials are never stored or transmitted by Android. They reside strictly inside the secure Cloud Run container.",
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Text(
                        text = "2. Deterministic JSON Contract: All /api/* endpoints strictly enforce application/json responses. Even in error states, JSON is returned—never HTML.",
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Text(
                        text = "3. Auto-Healing Fallbacks: The backend automatically handles Gemini quota limits with graceful model fallbacks and exponential backoff.",
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // Backend Endpoint Override (Debug / Diagnostics only)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SageCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SageSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = SagePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Production Backend URL",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Active Endpoint:\n$backendUrl",
                        color = SageGold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "The app connects to the official Cloud Run backend by default. Overrides must be valid HTTPS URLs (localhost is not accepted in production mode).",
                        color = SageTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            urlSavedNotification = null
                            urlValidationError = null
                        },
                        placeholder = { Text("https://your-backend.run.app", color = SageTextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            val trimmed = urlInput.trim()
                            if (trimmed.isEmpty()) {
                                onSaveBackendUrl("")
                                urlSavedNotification = "Restored official production backend."
                                urlInput = ""
                            } else if (!trimmed.startsWith("https://", ignoreCase = true)) {
                                urlValidationError = "Invalid URL: Only HTTPS endpoints are allowed."
                            } else if (trimmed.contains("localhost", ignoreCase = true)) {
                                urlValidationError = "Invalid URL: Localhost is not allowed for production mode."
                            } else {
                                onSaveBackendUrl(trimmed)
                                urlSavedNotification = "Backend URL updated."
                                urlInput = ""
                            }
                        }),
                        isError = urlValidationError != null,
                        supportingText = {
                            if (urlValidationError != null) {
                                Text(urlValidationError!!, color = SageError, fontSize = 11.sp)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SageTextPrimary,
                            unfocusedTextColor = SageTextPrimary,
                            focusedBorderColor = SagePrimary,
                            unfocusedBorderColor = SageCardBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_backend_url_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSaveBackendUrl("")
                                urlSavedNotification = "Restored official production backend."
                                urlValidationError = null
                                urlInput = ""
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("reset_backend_url_button")
                        ) {
                            Text("Reset Default", color = SageTextSecondary, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val trimmed = urlInput.trim()
                                if (trimmed.isEmpty()) {
                                    onSaveBackendUrl("")
                                    urlSavedNotification = "Restored official production backend."
                                    urlInput = ""
                                } else if (!trimmed.startsWith("https://", ignoreCase = true)) {
                                    urlValidationError = "Invalid URL: Only HTTPS endpoints are allowed."
                                } else if (trimmed.contains("localhost", ignoreCase = true)) {
                                    urlValidationError = "Invalid URL: Localhost is not allowed for production mode."
                                } else {
                                    onSaveBackendUrl(trimmed)
                                    urlSavedNotification = "Backend URL updated."
                                    urlInput = ""
                                }
                            },
                            enabled = urlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SagePrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_backend_url_button")
                        ) {
                            Text("Save URL")
                        }
                    }

                    if (urlSavedNotification != null) {
                        Text(
                            text = "✓ $urlSavedNotification Tap 'Run Connection Test' above to verify.",
                            color = SageSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DiagnosticRow(
    label: String,
    value: String,
    isSuccess: Boolean = false,
    isNeutral: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = SageTextSecondary,
            fontSize = 13.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val statusColor = when {
                isNeutral -> SageTextPrimary
                isSuccess -> SageSuccess
                else -> SageError
            }

            if (!isNeutral) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = value,
                color = statusColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                fontFamily = if (label.contains("Route") || label.contains("Type") || label.contains("Latency")) FontFamily.Monospace else FontFamily.Default
            )
        }
    }
}
