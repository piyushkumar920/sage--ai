package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.runtime.collectAsState
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
import com.example.data.studytools.AcademicContext
import com.example.data.studytools.StudyToolType
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.components.MessageBubble
import com.example.ui.components.ModeSelector
import com.example.ui.components.OfflineBanner
import com.example.ui.components.QuickActionChips
import com.example.ui.studytools.ChatAcademicContextHeader
import com.example.ui.studytools.ChatAttachedImagePreview
import com.example.ui.studytools.ChatStudyActionsToolbar
import com.example.ui.studytools.ChatStudyResultContainer
import com.example.ui.studytools.StudyToolUiState
import com.example.ui.studytools.StudyToolsViewModel
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
    academicContext: AcademicContext? = null,
    studyToolsViewModel: StudyToolsViewModel? = null,
    onSendMessage: (String) -> Unit,
    onRetryMessage: (Long) -> Unit,
    onEditMessage: ((Long, String) -> Unit)? = null,
    onRetryAi: ((Long) -> Unit)? = null,
    onModeChanged: (String) -> Unit,
    onOpenTopics: () -> Unit,
    onOpenRoadmap: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onTestConnection: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var editingMessageId by remember { mutableStateOf<Long?>(null) }
    var editingText by remember { mutableStateOf("") }
    var editErrorMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val studyToolState by studyToolsViewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(StudyToolUiState.Idle) }

    // Image Picker for Scan & Solve
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri
        }
    }

    // Auto-scroll on new messages or generation change
    LaunchedEffect(messages.size, isGenerating, studyToolState) {
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
                                text = academicContext?.courseName?.ifBlank { null }
                                    ?: activeTopic?.title
                                    ?: "Sage Learning",
                                color = SageTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
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
                            text = if (academicContext != null) "Syllabus Track • $currentMode" else "Mode: $currentMode",
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
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("chat_diagnostics_button")
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
                    containerColor = SageSurface.copy(alpha = 0.88f)
                )
            )
        }
    ) { innerPadding ->
        AmbientGlowBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Offline banner
                OfflineBanner(
                    isOffline = !isOnline,
                    onRetry = onTestConnection
                )

                // 1. Active Academic Context Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    ChatAcademicContextHeader(
                        academicContext = academicContext,
                        fallbackTitle = activeTopic?.title ?: "General Study",
                        currentMode = currentMode,
                        onChangeTopicClick = onOpenTopics
                    )
                }

                // 2. Chat Message List & In-Chat Study Tool Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty() && (studyToolState is StudyToolUiState.Idle)) {
                        // Empty State Welcome Card
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
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
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Sage AI Study Workspace",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageTextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Ask questions, generate curriculum notes, solve textbook problems with your camera, or review interactive flashcards.",
                                color = SageTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                level = GlassLevel.L2,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "QUICK STUDY PROMPTS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGold,
                                        letterSpacing = 0.6.sp
                                    )
                                    listOf(
                                        "Explain this module step-by-step with real-world examples",
                                        "Give me 5 key exam questions from this syllabus",
                                        "Compare the core algorithms and their time complexities"
                                    ).forEach { samplePrompt ->
                                        Text(
                                            text = "• \"$samplePrompt\"",
                                            color = SageTextPrimary,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                val isEditingThisMessage = (editingMessageId == msg.id)
                                MessageBubble(
                                    message = msg,
                                    onRetry = onRetryMessage,
                                    isEditing = isEditingThisMessage,
                                    editingText = if (isEditingThisMessage) editingText else "",
                                    onEditingTextChange = { newText ->
                                        if (isEditingThisMessage) {
                                            editingText = newText
                                            if (newText.isNotBlank()) {
                                                editErrorMessage = null
                                            }
                                        }
                                    },
                                    editErrorMessage = if (isEditingThisMessage) editErrorMessage else null,
                                    onStartEdit = { msgId, currentContent ->
                                        if (!isGenerating) {
                                            editingMessageId = msgId
                                            editingText = currentContent
                                            editErrorMessage = null
                                        }
                                    },
                                    onCancelEdit = {
                                        editingMessageId = null
                                        editingText = ""
                                        editErrorMessage = null
                                    },
                                    onSaveEdit = { msgId, editedTextContent ->
                                        val clean = editedTextContent.trim()
                                        if (clean.isEmpty()) {
                                            editErrorMessage = "Message cannot be empty."
                                        } else if (!isGenerating) {
                                            editingMessageId = null
                                            editErrorMessage = null
                                            onEditMessage?.invoke(msgId, clean)
                                        }
                                    },
                                    onRetryAi = { msgId ->
                                        if (!isGenerating) {
                                            onRetryAi?.invoke(msgId)
                                        }
                                    },
                                    isGenerating = isGenerating
                                )
                            }

                            // Active Study Tool Generation Result
                            if (studyToolsViewModel != null && studyToolState !is StudyToolUiState.Idle) {
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    ChatStudyResultContainer(
                                        viewModel = studyToolsViewModel,
                                        activeAcademicContext = academicContext,
                                        activeTopicTitle = activeTopic?.title ?: "Study Topic",
                                        onClose = { studyToolsViewModel.resetState() },
                                        onAskFollowUp = { prompt ->
                                            onSendMessage(prompt)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Attached Image Preview (For Scan & Solve)
                if (attachedImageUri != null) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        ChatAttachedImagePreview(
                            imageUri = attachedImageUri!!,
                            onRemove = { attachedImageUri = null }
                        )
                    }
                }

                // 4. Compact Study Actions Toolbar
                ChatStudyActionsToolbar(
                    onSelectTool = { toolType ->
                        val effectiveTopic = academicContext?.topic?.ifBlank { null }
                            ?: academicContext?.module?.ifBlank { null }
                            ?: activeTopic?.title?.ifBlank { null }
                            ?: "Core Syllabus"
                        val effectiveSubject = academicContext?.courseName?.ifBlank { null }
                            ?: academicContext?.courseCode?.ifBlank { null }
                            ?: "Academic Subject"

                        when (toolType) {
                            StudyToolType.SCAN_AND_SOLVE -> {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            StudyToolType.GENERATE_NOTES -> {
                                studyToolsViewModel?.generateNotes(
                                    topic = effectiveTopic,
                                    subject = effectiveSubject,
                                    prompt = "Generate comprehensive academic notes aligned with the official curriculum syllabus.",
                                    academicContext = academicContext
                                )
                            }
                            StudyToolType.FLASHCARDS -> {
                                studyToolsViewModel?.generateFlashcards(
                                    topic = effectiveTopic,
                                    subject = effectiveSubject,
                                    prompt = "Generate high-yield interactive flashcards covering key definitions, formulas, and concepts.",
                                    academicContext = academicContext
                                )
                            }
                            StudyToolType.MIND_MAP -> {
                                studyToolsViewModel?.generateMindMap(
                                    topic = effectiveTopic,
                                    subject = effectiveSubject,
                                    prompt = "Generate a hierarchical concept mind map tree.",
                                    academicContext = academicContext
                                )
                            }
                            StudyToolType.REVISION_SHEET -> {
                                studyToolsViewModel?.generateRevisionSheet(
                                    topic = effectiveTopic,
                                    subject = effectiveSubject,
                                    prompt = "Generate a rapid exam revision sheet with high-yield facts and misconceptions.",
                                    academicContext = academicContext
                                )
                            }
                            StudyToolType.FORMULA_SHEET -> {
                                studyToolsViewModel?.generateFormulaSheet(
                                    topic = effectiveTopic,
                                    subject = effectiveSubject,
                                    prompt = "Generate a formula sheet with variables, SI units, and application contexts.",
                                    academicContext = academicContext
                                )
                            }
                        }
                    }
                )

                // 5. Quick action chips
                QuickActionChips(
                    onActionSelected = { prompt ->
                        onSendMessage(prompt)
                    }
                )

                // 6. Input bar dock
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
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "AI tutor responses are grounded in JIS College curriculum syllabus.",
                        color = SageTextMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .testTag("chat_ai_disclaimer")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Scan & Solve camera button next to text input
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SageGlassL3)
                                .border(1.dp, SageGlassBorder, CircleShape)
                                .testTag("chat_camera_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Scan Problem",
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = if (attachedImageUri != null) "Ask question about image or tap send..." else if (currentMode == "SOCRATIC") "Reflect or answer here..." else "Ask Sage anything...",
                                    color = SageTextMuted,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (attachedImageUri != null) {
                                    val prompt = inputText.ifBlank { "Solve this academic problem step-by-step with explanation." }
                                    studyToolsViewModel?.solveImage(attachedImageUri!!, prompt, academicContext)
                                    attachedImageUri = null
                                    inputText = ""
                                } else if (inputText.isNotBlank() && !isGenerating) {
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

                        Spacer(modifier = Modifier.width(6.dp))

                        val canSend = (inputText.isNotBlank() || attachedImageUri != null) && !isGenerating
                        val sendInteractionSource = remember { MutableInteractionSource() }
                        val isSendPressed by sendInteractionSource.collectIsPressedAsState()

                        val sendScale by animateFloatAsState(
                            targetValue = if (isSendPressed) 0.92f else 1f,
                            animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
                            label = "send_scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .graphicsLayer {
                                    scaleX = sendScale
                                    scaleY = sendScale
                                }
                                .clip(CircleShape)
                                .background(
                                    if (canSend) {
                                        Brush.linearGradient(
                                            listOf(
                                                SagePrimaryStart,
                                                SagePrimary
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                SageGlassL2,
                                                SageGlassL1
                                            )
                                        )
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (canSend) SagePrimaryLight.copy(alpha = 0.6f) else SageGlassBorder,
                                    CircleShape
                                )
                                .clickable(
                                    enabled = canSend,
                                    interactionSource = sendInteractionSource,
                                    indication = null
                                ) {
                                    if (attachedImageUri != null) {
                                        val prompt = inputText.ifBlank { "Solve this academic problem step-by-step with explanation." }
                                        studyToolsViewModel?.solveImage(attachedImageUri!!, prompt, academicContext)
                                        attachedImageUri = null
                                        inputText = ""
                                    } else if (inputText.isNotBlank()) {
                                        onSendMessage(inputText)
                                        inputText = ""
                                    }
                                }
                                .testTag("chat_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SagePrimaryLight,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Message",
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
