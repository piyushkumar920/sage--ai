package com.example.ui.studytools

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.content.FileProvider
import com.example.data.studytools.SavedStudyToolEntity
import com.example.data.studytools.StudyToolType
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGlowEnd
import com.example.ui.theme.SageGlowStart
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageWarning
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudyToolsScreen(
    viewModel: StudyToolsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val savedTools by viewModel.savedTools.collectAsState()
    val currentCardIndex by viewModel.currentCardIndex.collectAsState()
    val isCardFlipped by viewModel.isCardFlipped.collectAsState()
    val capturedImageUri by viewModel.capturedImageUri.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Tools Hub, 1 = Saved Library
    var topicInput by remember { mutableStateOf(viewModel.contextTopic) }
    var subjectInput by remember { mutableStateOf(viewModel.contextSubject) }
    var promptInput by remember { mutableStateOf("") }

    // Camera launcher setup with FileProvider
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.setImageUri(tempCameraUri)
        }
    }

    // Photo picker launcher (Zero-permission Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setImageUri(uri)
        }
    }

    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("study_tools_screen")
        ) {
            // Screen Header
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("study_tools_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SageTextPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "Study Tools",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = if (viewModel.contextSubject.isNotBlank()) "Context: ${viewModel.contextSubject}" else "Academic Supercharger",
                            color = SageTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Online / Offline Indicator Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isOnline) SageSuccess.copy(alpha = 0.15f) else SageTextMuted.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (isOnline) SageSuccess.copy(alpha = 0.4f) else SageTextMuted.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isOnline) SageSuccess else SageTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isOnline) "Online" else "Saved Cache",
                            color = if (isOnline) SageSuccess else SageTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content Switching
            when (val state = uiState) {
                is StudyToolUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("study_tools_loading_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = SagePrimaryLight,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Synthesizing with Sage Academic AI...",
                                color = SageTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Structuring syllabus points, derivations, and pedagogy",
                                color = SageTextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                is StudyToolUiState.NotesReady -> {
                    NotesResultView(
                        notes = state.notes,
                        onClose = { viewModel.resetState() }
                    )
                }

                is StudyToolUiState.FlashcardsReady -> {
                    FlashcardsResultView(
                        flashcardsData = state.flashcards,
                        currentIndex = currentCardIndex,
                        isFlipped = isCardFlipped,
                        onFlip = { viewModel.flipCard() },
                        onPrev = { viewModel.prevCard() },
                        onNext = { viewModel.nextCard(state.flashcards.cards.size) },
                        onClose = { viewModel.resetState() }
                    )
                }

                is StudyToolUiState.MindMapReady -> {
                    MindMapResultView(
                        mindMapData = state.mindMap,
                        onClose = { viewModel.resetState() }
                    )
                }

                is StudyToolUiState.RevisionReady -> {
                    RevisionSheetResultView(
                        sheet = state.revision,
                        onClose = { viewModel.resetState() }
                    )
                }

                is StudyToolUiState.FormulasReady -> {
                    FormulaSheetResultView(
                        sheet = state.formulas,
                        onClose = { viewModel.resetState() }
                    )
                }

                is StudyToolUiState.SolutionReady -> {
                    SolutionResultView(
                        solution = state.solution,
                        onClose = { viewModel.resetState() }
                    )
                }

                else -> {
                    // Top Navigation Tabs (0 = Generator Hub, 1 = Saved Library)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SageGlassL1)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == 0) SagePrimaryStart else Color.Transparent)
                                .clickable { activeTab = 0 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tools Hub",
                                color = if (activeTab == 0) Color.White else SageTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == 1) SagePrimaryStart else Color.Transparent)
                                .clickable { activeTab = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Saved Library (${savedTools.size})",
                                color = if (activeTab == 1) Color.White else SageTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (activeTab == 0) {
                        // GENERATOR HUB
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (state is StudyToolUiState.Error) {
                                item {
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        level = GlassLevel.L2,
                                        borderColor = SageWarning.copy(alpha = 0.6f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Generation Error",
                                                color = SageWarning,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = state.message,
                                                color = SageTextPrimary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Curriculum Context banner if present
                            if (viewModel.contextTopic.isNotBlank()) {
                                item {
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        level = GlassLevel.L1,
                                        glowColor = SagePrimary.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = SagePrimaryLight
                                            )
                                            Column {
                                                Text(
                                                    text = "Curriculum Topic Linked",
                                                    color = SagePrimaryLight,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = viewModel.contextTopic,
                                                    color = SageTextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 6 Tool Action Cards
                            items(StudyToolType.values()) { tool ->
                                val isSelected = selectedTool == tool
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("study_tool_card_${tool.id}"),
                                    level = if (isSelected) GlassLevel.L3 else GlassLevel.L1,
                                    glowColor = if (isSelected) SagePrimaryStart.copy(alpha = 0.4f) else Color.Transparent,
                                    borderColor = if (isSelected) SagePrimaryStart else SageGlassBorder,
                                    onClick = { viewModel.selectTool(if (isSelected) null else tool) }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(text = tool.icon, fontSize = 24.sp)
                                                Column {
                                                    Text(
                                                        text = tool.title,
                                                        color = SageTextPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = tool.description,
                                                        color = SageTextSecondary,
                                                        fontSize = 12.sp,
                                                        lineHeight = 16.sp,
                                                        maxLines = 2
                                                    )
                                                }
                                            }
                                        }

                                        // Expandable Configuration Drawer when tool is tapped
                                        AnimatedVisibility(visible = isSelected) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 16.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                if (tool == StudyToolType.SCAN_AND_SOLVE) {
                                                    // Image Selection Buttons
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        GlassButton(
                                                            text = "Choose Photo",
                                                            onClick = {
                                                                photoPickerLauncher.launch(
                                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                                )
                                                            },
                                                            icon = Icons.Default.Image,
                                                            variant = GlassButtonVariant.Secondary,
                                                            modifier = Modifier.weight(1f)
                                                        )

                                                        GlassButton(
                                                            text = "Camera",
                                                            onClick = {
                                                                val tempFile = File.createTempFile("scan_", ".jpg", context.cacheDir)
                                                                val uri = FileProvider.getUriForFile(
                                                                    context,
                                                                    "${context.packageName}.fileprovider",
                                                                    tempFile
                                                                )
                                                                tempCameraUri = uri
                                                                cameraLauncher.launch(uri)
                                                            },
                                                            icon = Icons.Default.PhotoCamera,
                                                            variant = GlassButtonVariant.Secondary,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }

                                                    if (capturedImageUri != null) {
                                                        Text(
                                                            text = "✓ Image selected ready for scanning",
                                                            color = SageSuccess,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }

                                                    OutlinedTextField(
                                                        value = promptInput,
                                                        onValueChange = { promptInput = it },
                                                        label = { Text("Specific Question / Notes (Optional)", color = SageTextMuted) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = SagePrimaryStart,
                                                            unfocusedBorderColor = SageGlassBorder,
                                                            focusedTextColor = SageTextPrimary,
                                                            unfocusedTextColor = SageTextPrimary
                                                        ),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    GlassButton(
                                                        text = "Solve Step-by-Step",
                                                        onClick = {
                                                            val uri = capturedImageUri
                                                            if (uri != null) {
                                                                viewModel.solveImage(uri, promptInput)
                                                            }
                                                        },
                                                        enabled = capturedImageUri != null,
                                                        icon = Icons.Default.AutoAwesome,
                                                        variant = GlassButtonVariant.Primary,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(50.dp)
                                                    )
                                                } else {
                                                    // Text generation parameters
                                                    OutlinedTextField(
                                                        value = topicInput,
                                                        onValueChange = { topicInput = it },
                                                        label = { Text("Topic (e.g. Binary Search, Eigenvalues)", color = SageTextMuted) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = SagePrimaryStart,
                                                            unfocusedBorderColor = SageGlassBorder,
                                                            focusedTextColor = SageTextPrimary,
                                                            unfocusedTextColor = SageTextPrimary
                                                        ),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    OutlinedTextField(
                                                        value = subjectInput,
                                                        onValueChange = { subjectInput = it },
                                                        label = { Text("Subject (e.g. Data Structures, Linear Algebra)", color = SageTextMuted) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = SagePrimaryStart,
                                                            unfocusedBorderColor = SageGlassBorder,
                                                            focusedTextColor = SageTextPrimary,
                                                            unfocusedTextColor = SageTextPrimary
                                                        ),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    OutlinedTextField(
                                                        value = promptInput,
                                                        onValueChange = { promptInput = it },
                                                        label = { Text("Focus / Custom Directions (Optional)", color = SageTextMuted) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = SagePrimaryStart,
                                                            unfocusedBorderColor = SageGlassBorder,
                                                            focusedTextColor = SageTextPrimary,
                                                            unfocusedTextColor = SageTextPrimary
                                                        ),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    GlassButton(
                                                        text = "Generate ${tool.title}",
                                                        onClick = {
                                                            when (tool) {
                                                                StudyToolType.GENERATE_NOTES -> viewModel.generateNotes(topicInput, subjectInput, promptInput)
                                                                StudyToolType.FLASHCARDS -> viewModel.generateFlashcards(topicInput, subjectInput, promptInput)
                                                                StudyToolType.MIND_MAP -> viewModel.generateMindMap(topicInput, subjectInput, promptInput)
                                                                StudyToolType.REVISION_SHEET -> viewModel.generateRevisionSheet(topicInput, subjectInput, promptInput)
                                                                StudyToolType.FORMULA_SHEET -> viewModel.generateFormulaSheet(topicInput, subjectInput, promptInput)
                                                                else -> {}
                                                            }
                                                        },
                                                        enabled = topicInput.isNotBlank() || viewModel.contextTopic.isNotBlank(),
                                                        icon = Icons.Default.AutoAwesome,
                                                        variant = GlassButtonVariant.Primary,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(50.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    } else {
                        // SAVED LIBRARY TAB
                        if (savedTools.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = SageTextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "No saved study artefacts yet",
                                        color = SageTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Notes, flashcards, and formulas you create are saved here for offline study.",
                                        color = SageTextSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(savedTools) { item ->
                                    val icon = when (item.type) {
                                        StudyToolType.GENERATE_NOTES.id -> "📝"
                                        StudyToolType.FLASHCARDS.id -> "🗂"
                                        StudyToolType.MIND_MAP.id -> "🧠"
                                        StudyToolType.REVISION_SHEET.id -> "📄"
                                        StudyToolType.FORMULA_SHEET.id -> "🧮"
                                        else -> "📷"
                                    }
                                    val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(item.updatedAt))

                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        level = GlassLevel.L1,
                                        onClick = { viewModel.loadSavedTool(item) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(text = icon, fontSize = 24.sp)
                                                Column {
                                                    Text(
                                                        text = item.title,
                                                        color = SageTextPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = "${item.subject.ifBlank { "General" }} • $dateStr",
                                                        color = SageTextMuted,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }

                                            IconButton(onClick = { viewModel.deleteSavedTool(item.id) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = SageTextMuted
                                                )
                                            }
                                        }
                                    }
                                }

                                item { Spacer(modifier = Modifier.height(30.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
