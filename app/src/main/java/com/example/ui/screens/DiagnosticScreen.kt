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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiConfig
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
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
    apiKey: String = "",
    onRunTest: () -> Unit,
    onSaveBackendUrl: (String) -> Unit,
    onSaveApiKey: ((String) -> Unit)? = null,
    onClearApiKey: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    var urlInput by remember { mutableStateOf("") }
    var urlSavedNotification by remember { mutableStateOf(false) }

    var apiKeyInput by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var apiKeySavedNotification by remember { mutableStateOf(false) }

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
                            text = "Settings & Diagnostics",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("diagnostic_back_button")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SageTextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SageSurface)
            )
        },
        containerColor = SageBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 24 Specs: Status Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SageCardBorder, RoundedCornerShape(16.dp))
                    .testTag("diagnostic_status_card"),
                colors = CardDefaults.cardColors(containerColor = SageSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SYSTEM TELEMETRY",
                        color = SageGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    DiagnosticRow(
                        label = "Internet connection",
                        value = if (isOnline) "CONNECTED" else "OFFLINE",
                        isSuccess = isOnline
                    )

                    DiagnosticRow(
                        label = "AI service",
                        value = if (isAiConnected) "CONNECTED" else "FAILED",
                        isSuccess = isAiConnected
                    )

                    DiagnosticRow(
                        label = "Last request",
                        value = if (lastRequestSuccess) "SUCCESS" else "FAILED",
                        isSuccess = lastRequestSuccess
                    )

                    DiagnosticRow(
                        label = "Backend proxy",
                        value = if (isOnline) "ONLINE" else "OFFLINE",
                        isSuccess = isOnline
                    )

                    DiagnosticRow(
                        label = "Architecture",
                        value = if (apiKey.isNotBlank()) "Google Gemini Direct API" else "HTTPS Backend Proxy",
                        isSuccess = true
                    )

                    DiagnosticRow(
                        label = "Model",
                        value = GeminiConfig.GEMINI_MODEL,
                        isNeutral = true
                    )

                    DiagnosticRow(
                        label = "Last latency",
                        value = if (lastLatencyMs > 0) "${lastLatencyMs}ms" else "N/A",
                        isNeutral = true
                    )

                    DiagnosticRow(
                        label = "Last error",
                        value = lastError.ifEmpty { "None" },
                        isNeutral = lastError.isEmpty() || lastError.equals("None", ignoreCase = true),
                        isSuccess = lastError.isEmpty() || lastError.equals("None", ignoreCase = true)
                    )
                }
            }

            // Test AI connection button
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
                    .height(48.dp)
                    .testTag("run_diagnostic_test_button")
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Testing AI Connection...")
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Connection Test", fontWeight = FontWeight.Bold)
                }
            }

            // Gemini API Key Setup Card (Direct Connection)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (apiKey.isNotBlank()) SageSuccess.copy(alpha = 0.5f) else SageCardBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .testTag("api_key_setup_card"),
                colors = CardDefaults.cardColors(containerColor = SageSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = if (apiKey.isNotBlank()) SageSuccess else SageGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini API Key Setup",
                                color = SageTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (apiKey.isNotBlank()) SageSuccess.copy(alpha = 0.15f)
                                    else SagePrimary.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (apiKey.isNotBlank()) "DIRECT API ACTIVE" else "FREE TIER READY",
                                color = if (apiKey.isNotBlank()) SageSuccess else SagePrimaryLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (apiKey.isNotBlank())
                            "Direct Google Gemini API Key configured (${apiKey.take(4)}...${apiKey.takeLast(4)}). AI queries connect directly to Google without server proxy dependencies."
                        else
                            "Enter your Google AI Studio Gemini API Key for direct, 100% reliable connection on the Free Tier (no Cloud Run proxy or web login required).",
                        color = SageTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            apiKeySavedNotification = false
                        },
                        placeholder = {
                            Text(
                                text = if (apiKey.isNotBlank()) "Replace current key..." else "Paste AI Studio API key (AIzaSy...)",
                                color = SageTextMuted
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKey) "Hide key" else "Show key",
                                    tint = SageTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (apiKeyInput.isNotBlank() && onSaveApiKey != null) {
                                onSaveApiKey(apiKeyInput)
                                apiKeySavedNotification = true
                                apiKeyInput = ""
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SageTextPrimary,
                            unfocusedTextColor = SageTextPrimary,
                            focusedBorderColor = SageGold,
                            unfocusedBorderColor = SageCardBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_api_key_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        if (apiKey.isNotBlank() && onClearApiKey != null) {
                            OutlinedButton(
                                onClick = {
                                    onClearApiKey()
                                    apiKeySavedNotification = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("clear_api_key_button")
                            ) {
                                Text("Remove Key", color = SageError, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (apiKeyInput.isNotBlank() && onSaveApiKey != null) {
                                    onSaveApiKey(apiKeyInput)
                                    apiKeySavedNotification = true
                                    apiKeyInput = ""
                                }
                            },
                            enabled = apiKeyInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SageGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_api_key_button")
                        ) {
                            Text("Save API Key", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (apiKeySavedNotification) {
                        Text(
                            text = "✓ API Key saved. AI connection is now running in Direct Mode.",
                            color = SageSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Secure Backend Proxy Configuration
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
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = SagePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backend Server Configuration",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Current Proxy: $backendUrl",
                        color = SageGold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "All Gemini AI requests are securely routed through your cloud backend proxy. The Gemini API key is managed as a server-side secret and never bundled into this Android APK.",
                        color = SageTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            urlSavedNotification = false
                        },
                        placeholder = { Text("Enter HTTPS backend URL...", color = SageTextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (urlInput.isNotBlank()) {
                                onSaveBackendUrl(urlInput)
                                urlSavedNotification = true
                                urlInput = ""
                            }
                        }),
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
                                urlSavedNotification = true
                                urlInput = ""
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("reset_backend_url_button")
                        ) {
                            Text("Reset Default", color = SageTextSecondary, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (urlInput.isNotBlank()) {
                                    onSaveBackendUrl(urlInput)
                                    urlSavedNotification = true
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

                    if (urlSavedNotification) {
                        Text(
                            text = "✓ Backend URL updated. Tap 'Run Connection Test' above to verify.",
                            color = SageSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticRow(
    label: String,
    value: String,
    isSuccess: Boolean = true,
    isNeutral: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SageRaisedSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = SageTextSecondary,
            fontSize = 13.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isNeutral) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isSuccess) SageSuccess else SageError,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = value,
                color = if (isNeutral) SageTextPrimary else if (isSuccess) SageSuccess else SageError,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
