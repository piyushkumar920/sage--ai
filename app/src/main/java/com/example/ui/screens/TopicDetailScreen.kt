package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageWarning

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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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

            // Action Buttons Grid (4 Working Buttons)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Start Learning (Primary Call to Action)
                    GlassButton(
                        text = "Start Learning (Structured AI Session)",
                        onClick = onStartLearning,
                        icon = Icons.Default.School,
                        variant = GlassButtonVariant.Primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_learning_button")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Button 2: Ask Sage (Discussion)
                        GlassButton(
                            text = "Ask Sage",
                            onClick = onAskSage,
                            icon = Icons.Default.AutoAwesome,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("ask_sage_button")
                        )

                        // Button 3: Take Quiz
                        GlassButton(
                            text = "Take Quiz",
                            onClick = onTakeQuiz,
                            icon = Icons.Default.Quiz,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("take_quiz_button")
                        )
                    }

                    // Button 4: Study Tools (Curriculum Context AI Tools)
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

                    // Button 5: Mark Complete / Toggle Status
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
                                text = if (status == TopicStatus.COMPLETED) "Completed ✓ (Tap to Undo)" else "Mark as Complete",
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
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "WHAT YOU'LL LEARN",
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "• Core principles, lifecycle patterns, and foundational mechanics of ${node.title}.\n• Hands-on best practices, common pitfalls, and architecture tradeoffs.\n• Real-world scenario application verified through Sage's interactive tutor exercises.",
                            color = SageTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Section: Why It Matters
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "WHY IT MATTERS",
                            color = SageGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "In the $roadmapTitle curriculum, ${node.title} bridges theoretical fundamentals with production engineering. Mastering it prevents architectural debt and ensures predictable reliability at scale.",
                            color = SageTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Section: Prerequisites
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "PREREQUISITES & DEPENDENCIES",
                            color = SageTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        if (node.children.isNotEmpty()) {
                            Text(
                                text = "Unlocks subsequent modules: ${node.children.joinToString(", ")}",
                                color = SageTextSecondary,
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                text = "Foundational stage module in $roadmapTitle. Ready for direct study.",
                                color = SageTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Section: Recommended Resources
            item {
                Text(
                    text = "RECOMMENDED RESOURCES (${node.resources.size})",
                    color = SageTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (node.resources.isEmpty()) {
                item {
                    Text(
                        text = "No external links attached. Sage AI interactive lessons cover this topic completely.",
                        color = SageTextMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                items(node.resources) { resource ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (resource.url.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                            },
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = resource.title,
                                    color = SageTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (resource.type != null) {
                                    Text(
                                        text = resource.type.uppercase(),
                                        color = SagePrimaryLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open resource",
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
