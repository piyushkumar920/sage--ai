package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.roadmap.DevRoadmapNode
import com.example.data.roadmap.TopicStatus
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.theme.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

private data class TopicDetailStatusStyle(
    val bg: Color,
    val border: Color,
    val color: Color,
    val label: String
)

@Composable
fun TopicDetailScreen(
    node: DevRoadmapNode,
    roadmapTitle: String,
    status: TopicStatus,
    onBack: () -> Unit,
    onStartLearning: () -> Unit,
    onAskSage: () -> Unit,
    onTakeQuiz: () -> Unit,
    onToggleComplete: () -> Unit,
    onOpenStudyTools: (() -> Unit)? = null,
    onOpenFocusMode: (() -> Unit)? = null,
    onSimplify: (() -> Unit)? = null,
    onExamples: (() -> Unit)? = null,
    onPractice: (() -> Unit)? = null,
    onPyqs: (() -> Unit)? = null,
    onRevise: (() -> Unit)? = null,
    onScanAndSolve: (() -> Unit)? = null,
    topicAttachments: List<com.example.data.attachments.StudyAttachmentEntity> = emptyList(),
    onAddAttachments: ((List<android.net.Uri>) -> Unit)? = null,
    onRemoveAttachment: ((String) -> Unit)? = null,
    onRetryAttachment: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAddAttachments?.invoke(uris)
        }
    }

    val style = when (status) {
        TopicStatus.COMPLETED -> TopicDetailStatusStyle(
            bg = SageSuccess.copy(alpha = 0.15f),
            border = SageSuccess,
            color = SageSuccess,
            label = "COMPLETED"
        )
        TopicStatus.IN_PROGRESS -> TopicDetailStatusStyle(
            bg = SagePrimary.copy(alpha = 0.2f),
            border = SagePrimaryLight,
            color = SagePrimaryLight,
            label = "IN PROGRESS"
        )
        TopicStatus.NEEDS_REVIEW -> TopicDetailStatusStyle(
            bg = SageWarning.copy(alpha = 0.2f),
            border = SageWarning,
            color = SageWarning,
            label = "NEEDS REVIEW"
        )
        TopicStatus.NOT_STARTED -> TopicDetailStatusStyle(
            bg = SageRaisedSurface,
            border = SageCardBorder,
            color = SageTextMuted,
            label = "NOT STARTED"
        )
    }

    AmbientGlowBackground(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation Bar
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SageGlassL2)
                            .border(1.dp, SageGlassBorder, CircleShape)
                            .testTag("topic_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SageTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = roadmapTitle.uppercase(),
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = node.title,
                            color = SageTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Hero Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_hero_card"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SageGlassL3)
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.3f),
                                                    SageGlowEnd.copy(alpha = 0.5f)
                                                )
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = node.icon, fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = node.category.replaceFirstChar { it.uppercase() },
                                    color = SageTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(style.bg)
                                    .border(1.dp, style.border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = style.label,
                                    color = style.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = node.title,
                            color = SageTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        if (node.description.isNotBlank()) {
                            Text(
                                text = node.description,
                                color = SageTextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Stage 3: Attached Study Materials for Topic
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_attachments_card"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = SageGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TOPIC STUDY MATERIALS",
                                    color = SageGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            if (onAddAttachments != null) {
                                TextButton(
                                    onClick = {
                                        docPickerLauncher.launch(
                                            arrayOf(
                                                "application/pdf",
                                                "image/*",
                                                "text/plain",
                                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                                            )
                                        )
                                    },
                                    modifier = Modifier.testTag("attach_material_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = SagePrimaryLight, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Attach Notes", color = SagePrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (topicAttachments.isEmpty()) {
                            Text(
                                text = "No custom notes or slides attached yet. Attach PDFs, DOCX, or PPTX to ground AI learning sessions directly on your study material.",
                                color = SageTextMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                topicAttachments.forEach { att ->
                                    val (badgeText, badgeColor) = when {
                                        att.mimeType.contains("pdf") -> "PDF" to Color(0xFFE53935)
                                        att.mimeType.contains("image") -> "IMG" to Color(0xFF1E88E5)
                                        att.mimeType.contains("word") || att.mimeType.contains("docx") -> "DOCX" to Color(0xFF1565C0)
                                        att.mimeType.contains("presentation") || att.mimeType.contains("pptx") -> "PPTX" to Color(0xFFE65100)
                                        else -> "TXT" to Color(0xFF43A047)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SageGlassL1)
                                            .border(1.dp, SageGlassBorder, RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(badgeColor.copy(alpha = 0.2f))
                                                    .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(text = badgeText, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(text = att.originalFileName, color = SageTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                                Text(text = if (att.uploadStatus == "ATTACHED" || att.processingStatus == "COMPLETED") "Grounded for AI" else att.uploadStatus, color = SageTextMuted, fontSize = 10.sp)
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (att.uploadStatus.contains("FAILED")) {
                                                IconButton(onClick = { onRetryAttachment?.invoke(att.attachmentId) }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = SageGold, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            IconButton(onClick = { onRemoveAttachment?.invoke(att.attachmentId) }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = SageTextMuted, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Phase 4: 9 Interactive Learning Modes Studio Header
            item {
                Text(
                    text = "PHASE 4: 9 INTERACTIVE LEARNING MODES",
                    color = SageGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // 9 Learning Modes Grid / Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mode 1: Learn (Structured AI Session)
                    GlassButton(
                        text = "1. Learn (Structured AI Teaching Session)",
                        onClick = onStartLearning,
                        icon = Icons.Default.School,
                        variant = GlassButtonVariant.Primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("mode_learn_button")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Mode 2: Simplify
                        GlassButton(
                            text = "2. Simplify",
                            onClick = { onSimplify?.invoke() ?: onStartLearning() },
                            icon = Icons.Default.Lightbulb,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("mode_simplify_button")
                        )

                        // Mode 3: Examples
                        GlassButton(
                            text = "3. Examples",
                            onClick = { onExamples?.invoke() ?: onOpenStudyTools?.invoke() },
                            icon = Icons.Default.MenuBook,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("mode_examples_button")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Mode 4: Practice
                        GlassButton(
                            text = "4. Practice",
                            onClick = { onPractice?.invoke() ?: onTakeQuiz() },
                            icon = Icons.Default.EditNote,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("mode_practice_button")
                        )

                        // Mode 5: Quiz
                        GlassButton(
                            text = "5. Quiz",
                            onClick = onTakeQuiz,
                            icon = Icons.Default.Quiz,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("mode_quiz_button")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Mode 6: PYQs
                        GlassButton(
                            text = "6. PYQs",
                            onClick = { onPyqs?.invoke() ?: onTakeQuiz() },
                            icon = Icons.Default.HistoryEdu,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("mode_pyqs_button")
                        )

                        // Mode 7: Revise
                        GlassButton(
                            text = "7. Revise",
                            onClick = { onRevise?.invoke() ?: onOpenStudyTools?.invoke() },
                            icon = Icons.Default.Refresh,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("mode_revise_button")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Mode 8: Ask Sage
                        GlassButton(
                            text = "8. Ask Sage",
                            onClick = onAskSage,
                            icon = Icons.Default.AutoAwesome,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("mode_ask_sage_button")
                        )

                        // Mode 9: Scan & Solve
                        GlassButton(
                            text = "9. Scan & Solve",
                            onClick = { onScanAndSolve?.invoke() ?: onOpenStudyTools?.invoke() },
                            icon = Icons.Default.CameraAlt,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("mode_scan_solve_button")
                        )
                    }

                    // Study Tools & Focus Mode shortcuts
                    if (onOpenStudyTools != null) {
                        GlassButton(
                            text = "Study Tools (Notes, Cards, Maps & Sheets)",
                            onClick = onOpenStudyTools,
                            icon = Icons.Default.AutoAwesome,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("topic_study_tools_button")
                        )
                    }

                    if (onOpenFocusMode != null) {
                        GlassButton(
                            text = "🎯 Start Focus Session on This Topic",
                            onClick = onOpenFocusMode,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("topic_focus_mode_button")
                        )
                    }

                    // Explicit Completion Action (Do not mark complete automatically)
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mark_complete_button"),
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(14.dp),
                        onClick = { onToggleComplete() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (status == TopicStatus.COMPLETED) SageSuccess else SageTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (status == TopicStatus.COMPLETED) "Completed ✓ (Tap to Undo)" else "Mark Topic as Complete",
                                color = if (status == TopicStatus.COMPLETED) SageSuccess else SageTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Section: What You'll Learn
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Core Learning Objectives",
                            color = SageTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Master foundational principles and terminology\n• Work through structured examples and step-by-step solutions\n• Test comprehension using interactive quizzes and practice assessments\n• Build mastery for academic examinations and professional tests",
                            color = SageTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
