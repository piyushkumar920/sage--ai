package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.data.local.TopicEntity
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.components.MessageBubble
import com.example.ui.components.ModeSelector
import com.example.ui.components.OfflineBanner
import com.example.ui.components.QuickActionChips
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGlowEnd
import com.example.ui.theme.SageGlowStart
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    activeTopic: TopicEntity?,
    messages: List<MessageEntity>,
    isGenerating: Boolean,
    isOnline: Boolean,
    currentMode: String,
    streakDays: Int,
    onSendMessage: (String) -> Unit,
    onRetryMessage: (Long) -> Unit,
    onModeChanged: (String) -> Unit,
    onOpenTopics: () -> Unit,
    onOpenRoadmap: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onTestConnection: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll on new messages or generation change
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SageBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("chat_back_button")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SageTextPrimary
                            )
                        }
                    }
                },
                title = {
                    Column(
                        modifier = Modifier.clickable { onOpenTopics() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeTopic?.title ?: "Sage Learning",
                                color = SageTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Change Topic",
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "Mode: $currentMode",
                            color = if (currentMode == "SOCRATIC") SageGold else SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    // Visible Top-Right Roadmap Button
                    OutlinedButton(
                        onClick = onOpenRoadmap,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("chat_header_roadmap_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.4f),
                                    SageGold.copy(alpha = 0.8f)
                                )
                            )
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SageGold.copy(alpha = 0.12f),
                            contentColor = SageGold
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🗺 Roadmap",
                            color = SageGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Streak Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SageGlassL2)
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.25f),
                                        SageGold.copy(alpha = 0.5f)
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔥 ${streakDays}d",
                            color = SageGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Diagnostics button
                    IconButton(
                        onClick = onOpenDiagnostics,
                        modifier = Modifier.testTag("open_diagnostic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Diagnostics",
                            tint = SageTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SageGlassL2
                )
            )
        }
    ) { innerPadding ->
        AmbientGlowBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Mode Selector bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SageGlassL1)
                        .border(
                            1.dp,
                            SageGlassBorder,
                            RoundedCornerShape(0.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Tutoring Mode:",
                        color = SageTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    ModeSelector(
                        currentMode = currentMode,
                        onModeChanged = onModeChanged
                    )
                }

                // Offline banner
                OfflineBanner(
                    isOffline = !isOnline,
                    onRetry = onTestConnection
                )

                // Chat content area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        // Empty State Welcome Card
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                SagePrimaryStart,
                                                SagePrimary,
                                                SageAccent
                                            )
                                        )
                                    )
                                    .border(
                                        1.5.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.5f),
                                                SageGlowEnd
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Meet Sage",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageTextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Your personal Gemini AI tutor. Ask any question, explore complex ideas, or switch to Learning Mode for structured mastery.",
                                color = SageTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                level = GlassLevel.L2,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "PROMPTS TO GET STARTED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGold,
                                        letterSpacing = 0.6.sp
                                    )
                                    listOf(
                                        "Explain quantum entanglement with a simple analogy",
                                        "Teach me dynamic programming step by step",
                                        "What is the difference between TCP and UDP?"
                                    ).forEach { samplePrompt ->
                                        Text(
                                            text = "• \"$samplePrompt\"",
                                            color = SageTextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    onSendMessage(samplePrompt)
                                                }
                                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                MessageBubble(
                                    message = msg,
                                    onRetry = onRetryMessage
                                )
                            }
                        }
                    }
                }

                // Quick action chips
                QuickActionChips(
                    onActionSelected = { prompt ->
                        onSendMessage(prompt)
                    }
                )

                // Input bar dock
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SageGlassL2)
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    SageGlassBorderGlow.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.03f)
                                )
                            ),
                            RoundedCornerShape(0.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "AI can make mistakes. Please cross-check important information.",
                        color = SageTextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("chat_ai_disclaimer")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = if (currentMode == "SOCRATIC") "Reflect or answer here..." else "Ask Sage anything...",
                                    color = SageTextMuted,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (inputText.isNotBlank() && !isGenerating) {
                                    onSendMessage(inputText)
                                    inputText = ""
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SageTextPrimary,
                                unfocusedTextColor = SageTextPrimary,
                                focusedBorderColor = SagePrimaryLight,
                                unfocusedBorderColor = SageGlassBorder,
                                focusedContainerColor = SageGlassL3,
                                unfocusedContainerColor = SageGlassL2
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val canSend = inputText.isNotBlank() && !isGenerating
                        val sendInteractionSource = remember { MutableInteractionSource() }
                        val isSendPressed by sendInteractionSource.collectIsPressedAsState()

                        val sendScale by animateFloatAsState(
                            targetValue = if (isSendPressed) 0.88f else 1f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
                            label = "send_press"
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = sendScale
                                    scaleY = sendScale
                                }
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSend) {
                                        Brush.linearGradient(listOf(SagePrimaryStart, SagePrimary))
                                    } else {
                                        Brush.linearGradient(listOf(SageGlassL1, SageGlassL2))
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (canSend) {
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.4f),
                                                SageGlowEnd.copy(alpha = 0.6f)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(listOf(SageGlassBorder, SageGlassBorder))
                                    },
                                    CircleShape
                                )
                                .clickable(
                                    interactionSource = sendInteractionSource,
                                    indication = null,
                                    enabled = canSend
                                ) {
                                    if (canSend) {
                                        onSendMessage(inputText)
                                        inputText = ""
                                    }
                                }
                                .testTag("send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    color = SageGold,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (canSend) Color.White else SageTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
