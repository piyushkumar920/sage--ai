package com.example.ui.studytools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.studytools.FormulaItem
import com.example.data.studytools.MindMapNode
import com.example.data.studytools.StudyFlashcardsData
import com.example.data.studytools.StudyFormulaSheetData
import com.example.data.studytools.StudyMindMapData
import com.example.data.studytools.StudyNotesData
import com.example.data.studytools.StudyRevisionSheetData
import com.example.data.studytools.StudySolutionData
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.components.MathFormulaCard
import com.example.ui.components.MathText
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGlowEnd
import com.example.ui.theme.SageGlowStart
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageWarning

// ==========================================
// 1. NOTES RESULT VIEW
// ==========================================
@Composable
fun NotesResultView(
    notes: StudyNotesData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_notes_result_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SageTextSecondary)
                }
                Text(
                    text = "📝 Academic Notes",
                    color = SageTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Box(modifier = Modifier.size(48.dp))
            }
        }

        if (notes.beyondSyllabus) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L2,
                    borderColor = SageWarning.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SageWarning)
                        Text(
                            text = "Advanced Enrichment: Some concepts here extend beyond basic syllabus boundaries.",
                            color = SageWarning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                level = GlassLevel.L2,
                glowColor = SagePrimary.copy(alpha = 0.2f)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = notes.title,
                        color = SageTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = notes.summary,
                        color = SageTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        items(notes.sections) { section ->
            GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.L1) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = section.heading,
                        color = SagePrimaryLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = section.content,
                        color = SageTextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    if (section.keyPoints.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            section.keyPoints.forEach { pt ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(text = "•", color = SageGold, fontWeight = FontWeight.Bold)
                                    Text(text = pt, color = SageTextSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (notes.importantTerms.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.L2) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Key Terminology & Definitions",
                            color = SageGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        notes.importantTerms.forEach { term ->
                            Text(text = "📖 $term", color = SageTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        if (notes.examples.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.L1) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Worked Examples & Context",
                            color = SageSuccess,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        notes.examples.forEach { ex ->
                            Text(text = "💡 $ex", color = SageTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 2. FLASHCARDS RESULT VIEW (3D FLIP CARD)
// ==========================================
@Composable
fun FlashcardsResultView(
    flashcardsData: StudyFlashcardsData,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = flashcardsData.cards.size
    val currentCard = flashcardsData.cards.getOrNull(currentIndex)

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flashcard_flip"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_flashcards_result_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SageTextSecondary)
            }
            Text(
                text = "${flashcardsData.title} (${currentIndex + 1}/$total)",
                color = SageTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Box(modifier = Modifier.size(48.dp))
        }

        if (currentCard != null) {
            // Interactive 3D Flip Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (rotation > 90f) SageGlassL3 else SageGlassL2)
                    .border(
                        1.5.dp,
                        if (rotation > 90f) SageSuccess.copy(alpha = 0.5f) else SageGlassBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onFlip() }
                    .padding(24.dp)
                    .testTag("flashcard_card_box"),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "QUESTION",
                            color = SagePrimaryLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentCard.front,
                            color = SageTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Tap to Reveal Answer ↷",
                            color = SageTextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    // Back side (must invert rotationY to avoid backwards text)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        Text(
                            text = "ANSWER",
                            color = SageSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentCard.back,
                            color = SageTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        if (currentCard.hint.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "💡 Hint: ${currentCard.hint}",
                                color = SageGold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Carousel Navigation Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassButton(
                    text = "Previous",
                    onClick = onPrev,
                    icon = Icons.Default.NavigateBefore,
                    variant = GlassButtonVariant.Secondary,
                    modifier = Modifier.width(130.dp)
                )
                Text(
                    text = "${currentIndex + 1} / $total",
                    color = SageTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                GlassButton(
                    text = "Next",
                    onClick = onNext,
                    icon = Icons.Default.NavigateNext,
                    variant = GlassButtonVariant.Primary,
                    modifier = Modifier.width(130.dp)
                )
            }
        }
    }
}

// ==========================================
// 3. MIND MAP RESULT VIEW (INTERACTIVE TREE)
// ==========================================
@Composable
fun MindMapResultView(
    mindMapData: StudyMindMapData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_mindmap_result_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SageTextSecondary)
            }
            Text(
                text = "🧠 ${mindMapData.title}",
                color = SageTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            IconButton(onClick = { scale = if (scale == 1f) 1.25f else 1f }) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Zoom",
                    tint = SageGold
                )
            }
        }

        Text(
            text = "Tap nodes to expand sub-branches or view detailed pedagogical breakdowns.",
            color = SageTextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.8f, 2.0f)
                    }
                }
                .graphicsLayer(scaleX = scale, scaleY = scale),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                MindMapNodeComposable(node = mindMapData.root, depth = 0)
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun MindMapNodeComposable(
    node: MindMapNode,
    depth: Int,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(depth < 2) }

    val indent = (depth * 20).dp
    val nodeColor = when (depth) {
        0 -> SagePrimaryStart
        1 -> SagePrimaryLight
        2 -> SageGold
        else -> SageSuccess
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = indent, top = 4.dp, bottom = 4.dp)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (node.children.isNotEmpty()) isExpanded = !isExpanded },
            level = if (depth == 0) GlassLevel.L3 else GlassLevel.L1,
            glowColor = nodeColor.copy(alpha = 0.25f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(nodeColor)
                    )
                    Column {
                        Text(
                            text = node.label,
                            color = SageTextPrimary,
                            fontWeight = if (depth == 0) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = if (depth == 0) 16.sp else 14.sp
                        )
                        if (node.description.isNotBlank()) {
                            Text(
                                text = node.description,
                                color = SageTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                if (node.children.isNotEmpty()) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = SageTextSecondary
                    )
                }
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                node.children.forEach { child ->
                    MindMapNodeComposable(node = child, depth = depth + 1)
                }
            }
        }
    }
}

// ==========================================
// 4. REVISION SHEET RESULT VIEW
// ==========================================
@Composable
fun RevisionSheetResultView(
    sheet: StudyRevisionSheetData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_revision_result_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SageTextSecondary)
                }
                Text(
                    text = "⚡ Last-Minute Revision Sheet",
                    color = SageTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Box(modifier = Modifier.size(48.dp))
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.L2) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = sheet.title,
                        color = SageTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "High-yield summary engineered for active recall right before exams.",
                        color = SageTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (sheet.lastMinuteRevisionPoints.isNotEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L2,
                    glowColor = SageGold.copy(alpha = 0.3f),
                    borderColor = SageGold.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🚨 5-Minute Exam Drill Points",
                            color = SageGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        sheet.lastMinuteRevisionPoints.forEach { pt ->
                            Text(text = "⚡ $pt", color = SageTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        if (sheet.coreConcepts.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.L1) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Core Concepts", color = SagePrimaryLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        sheet.coreConcepts.forEach { c ->
                            Text(text = "• $c", color = SageTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        if (sheet.commonMistakes.isNotEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    borderColor = SageWarning.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "⚠️ Common Traps & Exam Mistakes", color = SageWarning, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        sheet.commonMistakes.forEach { m ->
                            Text(text = "❌ $m", color = SageTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        if (sheet.definitions.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.L1) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Key Definitions", color = SageSuccess, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        sheet.definitions.forEach { d ->
                            Text(text = "📌 $d", color = SageTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ==========================================
// 5. FORMULA SHEET RESULT VIEW
// ==========================================
@Composable
fun FormulaSheetResultView(
    sheet: StudyFormulaSheetData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_formula_result_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SageTextSecondary)
                }
                Text(
                    text = "🧮 ${sheet.title}",
                    color = SageTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Box(modifier = Modifier.size(48.dp))
            }
        }

        if (sheet.notes.isNotBlank()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.L1) {
                    Text(
                        text = sheet.notes,
                        color = SageTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        items(sheet.formulas) { item ->
            MathFormulaCard(
                formula = item.formula,
                title = item.name,
                units = item.units.ifBlank { null },
                variables = item.variables,
                usage = item.usageExplanation.ifBlank { null },
                example = item.example.ifBlank { null }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ==========================================
// 6. SCAN & SOLVE RESULT VIEW
// ==========================================
@Composable
fun SolutionResultView(
    solution: StudySolutionData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_solution_result_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SageTextSecondary)
                }
                Text(
                    text = "📷 Step-by-Step Solution",
                    color = SageTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Box(modifier = Modifier.size(48.dp))
            }
        }

        if (!solution.isClear) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L2,
                    borderColor = SageWarning.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Image Quality Unclear",
                            color = SageWarning,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        MathText(
                            text = solution.answer,
                            color = SageTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.L1) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (solution.subject.isNotBlank()) {
                            Text(
                                text = "Subject: ${solution.subject}",
                                color = SagePrimaryLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Detected Question:",
                            color = SageTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        MathText(
                            text = solution.problem,
                            color = SageTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            items(solution.steps) { step ->
                GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.L2) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SagePrimaryStart),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${step.step}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = step.title,
                                color = SageTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        MathText(
                            text = step.explanation,
                            color = SageTextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            if (solution.finalAnswer.isNotBlank()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        level = GlassLevel.L3,
                        glowColor = SageSuccess.copy(alpha = 0.35f),
                        borderColor = SageSuccess.copy(alpha = 0.7f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "FINAL ANSWER",
                                color = SageSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                            if (solution.finalAnswer.contains("\\") || solution.finalAnswer.contains("^") || solution.finalAnswer.contains("_")) {
                                MathFormulaCard(formula = solution.finalAnswer)
                            } else {
                                MathText(
                                    text = solution.finalAnswer,
                                    color = SageTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
