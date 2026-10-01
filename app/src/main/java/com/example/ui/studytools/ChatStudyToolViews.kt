package com.example.ui.studytools

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.studytools.AcademicContext
import com.example.data.studytools.MindMapNode
import com.example.data.studytools.StudyFlashcardsData
import com.example.data.studytools.StudyFormulaSheetData
import com.example.data.studytools.StudyMindMapData
import com.example.data.studytools.StudyNotesData
import com.example.data.studytools.StudyRevisionSheetData
import com.example.data.studytools.StudySolutionData
import com.example.data.studytools.StudyToolType
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.components.MathFormulaCard
import com.example.ui.components.MathText
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageError
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
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageWarning

/**
 * 1. CHAT ACADEMIC CONTEXT HEADER
 * Displays current course, semester, department, topic, and a prominent "Change Topic" trigger.
 */
@Composable
fun ChatAcademicContextHeader(
    academicContext: AcademicContext?,
    fallbackTitle: String,
    currentMode: String,
    onChangeTopicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        level = GlassLevel.L2,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SageGlassL3)
                        .border(1.dp, SagePrimaryLight.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (academicContext != null) "📚" else "💬",
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = academicContext?.courseName?.ifBlank { null }
                            ?: academicContext?.topic?.ifBlank { null }
                            ?: fallbackTitle,
                        color = SageTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val subline = buildString {
                        if (academicContext != null) {
                            if (academicContext.semester != null) append("Sem ${academicContext.semester} • ")
                            if (academicContext.module.isNotBlank()) append(academicContext.module)
                            else if (academicContext.department.isNotBlank()) append(academicContext.department)
                        } else {
                            append("General AI Tutor • $currentMode Mode")
                        }
                    }

                    Text(
                        text = subline,
                        color = SageTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Change Topic Button
            OutlinedButton(
                onClick = onChangeTopicClick,
                modifier = Modifier
                    .height(32.dp)
                    .testTag("chat_header_change_topic_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.25f),
                            SagePrimaryLight.copy(alpha = 0.6f)
                        )
                    )
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SageGlassL2,
                    contentColor = SagePrimaryLight
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = SagePrimaryLight
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Topic",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 2. COMPACT STUDY ACTIONS TOOLBAR
 * Clean horizontal strip docked right above the input bar with fast access to all 6 study tools.
 */
@Composable
fun ChatStudyActionsToolbar(
    onSelectTool: (StudyToolType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StudyToolType.values().forEach { tool ->
            ChatStudyToolChip(
                tool = tool,
                onClick = { onSelectTool(tool) }
            )
        }
    }
}

@Composable
private fun ChatStudyToolChip(
    tool: StudyToolType,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "tool_chip_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(SageGlassL2)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.2f),
                        SageGlassBorderGlow.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("chat_tool_chip_${tool.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = tool.icon,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tool.title,
                color = SageTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 3. ATTACHED IMAGE PREVIEW
 * Shows selected question/diagram thumbnail for Scan & Solve before sending.
 */
@Composable
fun ChatAttachedImagePreview(
    imageUri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SageGlassL2)
            .border(1.dp, SagePrimaryLight.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected Problem Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📷 Image Attached for Scan & Solve",
                    color = SagePrimaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap send or type specific question below",
                    color = SageTextSecondary,
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Image",
                    tint = SageTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 4. IN-CHAT STUDY RESULT CONTAINER
 * Renders generated Notes, Flashcards, Mind Map, Revision Sheet, Formula Sheet, or Scan Solution directly in Chat.
 */
@Composable
fun ChatStudyResultContainer(
    viewModel: StudyToolsViewModel,
    activeAcademicContext: AcademicContext?,
    activeTopicTitle: String,
    onClose: () -> Unit,
    onAskFollowUp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chat_study_result_container"),
        level = GlassLevel.L3,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Container Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(SagePrimaryStart.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (state) {
                                is StudyToolUiState.NotesReady -> "📝"
                                is StudyToolUiState.FlashcardsReady -> "🗂"
                                is StudyToolUiState.MindMapReady -> "🧠"
                                is StudyToolUiState.RevisionReady -> "📄"
                                is StudyToolUiState.FormulasReady -> "🧮"
                                is StudyToolUiState.SolutionReady -> "📷"
                                is StudyToolUiState.Loading -> "⏳"
                                else -> "✨"
                            },
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = when (state) {
                                is StudyToolUiState.NotesReady -> "Academic Study Notes"
                                is StudyToolUiState.FlashcardsReady -> "Interactive Flashcards"
                                is StudyToolUiState.MindMapReady -> "Concept Mind Map"
                                is StudyToolUiState.RevisionReady -> "Exam Revision Sheet"
                                is StudyToolUiState.FormulasReady -> "Curriculum Formula Sheet"
                                is StudyToolUiState.SolutionReady -> "Scan & Solve Solution"
                                is StudyToolUiState.Loading -> "Generating Curriculum Artefact..."
                                is StudyToolUiState.Error -> "Study Tool Issue"
                                else -> "Study Tool Workspace"
                            },
                            color = SageTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = activeAcademicContext?.courseName?.ifBlank { null }
                                ?: activeAcademicContext?.topic?.ifBlank { null }
                                ?: activeTopicTitle,
                            color = SageGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Tool",
                        tint = SageTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body State Rendering
            when (val s = state) {
                is StudyToolUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = SagePrimaryLight,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Synthesizing official curriculum context with Gemini AI...",
                            color = SageTextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is StudyToolUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SageError.copy(alpha = 0.12f))
                            .border(1.dp, SageError.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = s.message,
                            color = SageError,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val effectiveTopic = activeAcademicContext?.topic?.ifBlank { null } ?: activeTopicTitle
                                val effectiveSubject = activeAcademicContext?.courseName?.ifBlank { null } ?: "Curriculum"
                                viewModel.generateNotes(effectiveTopic, effectiveSubject, academicContext = activeAcademicContext)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Retry Generation", fontSize = 11.sp)
                        }
                    }
                }

                is StudyToolUiState.NotesReady -> {
                    InChatNotesContent(notes = s.notes, onAskFollowUp = onAskFollowUp)
                }

                is StudyToolUiState.FlashcardsReady -> {
                    val currentIndex by viewModel.currentCardIndex.collectAsState()
                    val isFlipped by viewModel.isCardFlipped.collectAsState()
                    InChatFlashcardsContent(
                        flashcards = s.flashcards,
                        currentIndex = currentIndex,
                        isFlipped = isFlipped,
                        onFlip = { viewModel.flipCard() },
                        onNext = { viewModel.nextCard(s.flashcards.cards.size) },
                        onPrev = { viewModel.prevCard() },
                        onAskFollowUp = onAskFollowUp
                    )
                }

                is StudyToolUiState.MindMapReady -> {
                    InChatMindMapContent(mindMap = s.mindMap, onAskFollowUp = onAskFollowUp)
                }

                is StudyToolUiState.RevisionReady -> {
                    InChatRevisionContent(revision = s.revision, onAskFollowUp = onAskFollowUp)
                }

                is StudyToolUiState.FormulasReady -> {
                    InChatFormulaContent(formulas = s.formulas, onAskFollowUp = onAskFollowUp)
                }

                is StudyToolUiState.SolutionReady -> {
                    InChatSolutionContent(solution = s.solution, onAskFollowUp = onAskFollowUp)
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun InChatNotesContent(
    notes: StudyNotesData,
    onAskFollowUp: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (notes.summary.isNotBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SageGlassL2)
                        .border(1.dp, SageGlassBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "EXECUTIVE SUMMARY", color = SageGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = notes.summary, color = SageTextPrimary, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
        }

        items(notes.sections) { section ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SageGlassL1)
                    .border(1.dp, SageGlassBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = section.heading, color = SagePrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = section.content, color = SageTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                    if (section.keyPoints.isNotEmpty()) {
                        section.keyPoints.forEach { kp ->
                            Text(text = "• $kp", color = SageGold, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        if (notes.importantTerms.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "IMPORTANT TERMS", color = SageGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    notes.importantTerms.forEach { term ->
                        Text(text = "• $term", color = SageTextPrimary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InChatFlashcardsContent(
    flashcards: StudyFlashcardsData,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onAskFollowUp: (String) -> Unit
) {
    val cards = flashcards.cards
    if (cards.isEmpty()) {
        Text("No flashcards generated", color = SageTextMuted, fontSize = 12.sp)
        return
    }

    val currentCard = cards.getOrNull(currentIndex) ?: cards.first()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Counter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Card ${currentIndex + 1} of ${cards.size}",
                color = SageGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isFlipped) "BACK (ANSWER)" else "FRONT (PROMPT)",
                color = SagePrimaryLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Interactive Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isFlipped) {
                        Brush.linearGradient(
                            listOf(
                                SagePrimaryStart.copy(alpha = 0.35f),
                                SagePrimary.copy(alpha = 0.25f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                SageGlassL2,
                                SageGlassL3
                            )
                        )
                    }
                )
                .border(
                    1.dp,
                    if (isFlipped) SagePrimaryLight else SageGlassBorder,
                    RoundedCornerShape(14.dp)
                )
                .clickable { onFlip() }
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isFlipped) currentCard.back else currentCard.front,
                    color = SageTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                if (!isFlipped && currentCard.hint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💡 Hint: ${currentCard.hint}",
                        color = SageTextMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap card to flip",
                    color = SageGold.copy(alpha = 0.8f),
                    fontSize = 9.sp
                )
            }
        }

        // Card Navigation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrev,
                enabled = currentIndex > 0,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SageGlassL2)
            ) {
                Icon(
                    imageVector = Icons.Default.NavigateBefore,
                    contentDescription = "Previous Card",
                    tint = if (currentIndex > 0) SageTextPrimary else SageTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            OutlinedButton(
                onClick = onFlip,
                modifier = Modifier.height(30.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SagePrimaryLight),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flip", fontSize = 11.sp)
            }

            IconButton(
                onClick = onNext,
                enabled = currentIndex < cards.size - 1,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SageGlassL2)
            ) {
                Icon(
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = "Next Card",
                    tint = if (currentIndex < cards.size - 1) SageTextPrimary else SageTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun InChatMindMapContent(
    mindMap: StudyMindMapData,
    onAskFollowUp: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SagePrimaryStart.copy(alpha = 0.35f))
                    .border(1.dp, SagePrimaryLight, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "ROOT CONCEPT", color = SageGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = mindMap.root.label.ifBlank { mindMap.title }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (mindMap.root.description.isNotBlank()) {
                        Text(text = mindMap.root.description, color = SageTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        items(mindMap.root.children) { node ->
            MindMapNodeCard(node = node)
        }
    }
}

@Composable
private fun MindMapNodeCard(node: MindMapNode) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SageGlassL2)
            .border(1.dp, SageGlassBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "🌿 ${node.label}", color = SagePrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (node.description.isNotBlank()) {
                Text(text = node.description, color = SageTextSecondary, fontSize = 11.sp)
            }
            if (node.children.isNotEmpty()) {
                node.children.forEach { child ->
                    Text(text = "   └ ${child.label}: ${child.description}", color = SageTextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun InChatRevisionContent(
    revision: StudyRevisionSheetData,
    onAskFollowUp: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (revision.keyFacts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "HIGH-YIELD EXAM FACTS", color = SageGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    revision.keyFacts.forEach { fact ->
                        Text(text = "⭐ $fact", color = SageTextPrimary, fontSize = 11.sp)
                    }
                }
            }
        }

        if (revision.definitions.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "KEY DEFINITIONS", color = SagePrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    revision.definitions.forEach { def ->
                        Text(text = "• $def", color = SageTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        if (revision.commonMistakes.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "COMMON PITFALLS & MISCONCEPTIONS", color = SageWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    revision.commonMistakes.forEach { mis ->
                        Text(text = "⚠️ $mis", color = SageTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        if (revision.lastMinuteRevisionPoints.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "LAST-MINUTE REVISION POINTS", color = SageGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    revision.lastMinuteRevisionPoints.forEach { pt ->
                        Text(text = "⚡ $pt", color = SageTextPrimary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InChatFormulaContent(
    formulas: StudyFormulaSheetData,
    onAskFollowUp: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(formulas.formulas) { formula ->
            MathFormulaCard(
                formula = formula.formula,
                title = formula.name,
                units = formula.units.ifBlank { null },
                variables = formula.variables,
                usage = formula.usageExplanation.ifBlank { null },
                example = formula.example.ifBlank { null }
            )
        }
    }
}

@Composable
private fun InChatSolutionContent(
    solution: StudySolutionData,
    onAskFollowUp: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SageGlassL2)
                    .border(1.dp, SageGlassBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "PROBLEM RECOGNITION", color = SageGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    MathText(text = solution.problem, color = SageTextPrimary, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }

        items(solution.steps) { step ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SageGlassL1)
                    .border(1.dp, SageGlassBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Step ${step.step}: ${step.title}", color = SagePrimaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    MathText(text = step.explanation, color = SageTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }

        if (solution.finalAnswer.isNotBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SageSuccess.copy(alpha = 0.15f))
                        .border(1.dp, SageSuccess.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "FINAL ANSWER", color = SageSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (solution.finalAnswer.contains("\\") || solution.finalAnswer.contains("^") || solution.finalAnswer.contains("_")) {
                            MathFormulaCard(formula = solution.finalAnswer)
                        } else {
                            MathText(text = solution.finalAnswer, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
