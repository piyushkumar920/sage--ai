package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TopicProgressEntity
import com.example.data.roadmap.DevRoadmapDetail
import com.example.data.roadmap.DevRoadmapNode
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoadmapScreen(
    activeRoadmapDetail: DevRoadmapDetail?,
    progressList: List<TopicProgressEntity>,
    onBackToChat: () -> Unit,
    onContinueLearning: (DevRoadmapNode) -> Unit,
    onViewFullRoadmap: (String) -> Unit,
    onPracticeWeakTopics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roadmap = activeRoadmapDetail
    val roadmapTitle = roadmap?.title ?: "Full Stack Web Development"
    val roadmapId = roadmap?.id ?: "fullstack"
    val roadmapIcon = roadmap?.icon ?: "🌐"

    val statusMap = remember(progressList) {
        progressList.associate { it.nodeId to it.status }
    }
    val progressMap = remember(progressList) {
        progressList.associateBy { it.nodeId }
    }

    val totalNodes = roadmap?.nodes?.size ?: 0
    val completedCount = progressList.count { it.status == "COMPLETED" }
    val inProgressCount = progressList.count { it.status == "IN_PROGRESS" }
    val needsReviewCount = progressList.count { it.status == "NEEDS_REVIEW" }
    val remainingCount = if (totalNodes > 0) (totalNodes - completedCount).coerceAtLeast(0) else 0
    val percentComplete = if (totalNodes > 0) ((completedCount * 100) / totalNodes).coerceIn(0, 100) else 0

    // Filter topics studied in chat
    val studiedInChatList = remember(progressList) {
        progressList.filter { it.studiedInChat || it.sessionsCount > 0 || it.status != "NOT_STARTED" }
            .sortedWith(compareByDescending<TopicProgressEntity> { it.lastStudiedAt ?: 0L }
                .thenByDescending { it.updatedAt })
    }

    // Identify current topic
    val currentTopicNode = remember(roadmap, statusMap) {
        roadmap?.nodes?.find { statusMap[it.id] == "IN_PROGRESS" }
            ?: roadmap?.nodes?.find { statusMap[it.id] == "NEEDS_REVIEW" }
            ?: roadmap?.nodes?.find { statusMap[it.id] != "COMPLETED" }
            ?: roadmap?.nodes?.firstOrNull()
    }

    // Identify next recommended topic
    val nextRecommendedNode = remember(roadmap, statusMap, currentTopicNode) {
        roadmap?.nodes?.find { node ->
            node.id != currentTopicNode?.id && (statusMap[node.id] == null || statusMap[node.id] == "NOT_STARTED")
        } ?: currentTopicNode
    }

    // List of still to learn topics
    val stillToLearnNodes = remember(roadmap, statusMap) {
        roadmap?.nodes?.filter { statusMap[it.id] != "COMPLETED" } ?: emptyList()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Your Learning Progress",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Chat Roadmap & Real-time Progress",
                            color = SagePrimaryLight,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToChat,
                        modifier = Modifier.testTag("chat_roadmap_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Chat",
                            tint = SageTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SageSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ==========================================
            // 1. CURRENT ROADMAP PROGRESS CARD
            // ==========================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_roadmap_overview_card"),
                    colors = CardDefaults.cardColors(containerColor = SageRaisedSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SageCardBorder))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CURRENT ROADMAP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SagePrimaryLight,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$percentComplete% Complete",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SageGold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = roadmapIcon,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = roadmapTitle,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageTextPrimary
                                )
                                Text(
                                    text = "Single source of truth via Sage Room DB",
                                    fontSize = 11.sp,
                                    color = SageTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dual Visual Progress Bar
                        LinearProgressIndicator(
                            progress = { percentComplete / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = SagePrimary,
                            trackColor = SageSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Compact text visual representation
                        val filledChars = ((percentComplete * 18) / 100).coerceIn(0, 18)
                        val emptyChars = (18 - filledChars).coerceAtLeast(0)
                        val visualBar = "█".repeat(filledChars) + "░".repeat(emptyChars)

                        Text(
                            text = visualBar,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = SagePrimaryLight,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = SageCardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Studied", fontSize = 11.sp, color = SageTextMuted)
                                Text(
                                    text = "$percentComplete%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageSuccess
                                )
                            }
                            Column {
                                Text(text = "Remaining", fontSize = 11.sp, color = SageTextMuted)
                                Text(
                                    text = "${100 - percentComplete}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageTextSecondary
                                )
                            }
                            Column {
                                Text(text = "Completed", fontSize = 11.sp, color = SageTextMuted)
                                Text(
                                    text = "$completedCount topics",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SagePrimaryLight
                                )
                            }
                            Column {
                                Text(text = "In Progress", fontSize = 11.sp, color = SageTextMuted)
                                Text(
                                    text = "$inProgressCount",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGold
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. WHAT THE USER HAS STUDIED IN CHAT
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STUDIED IN CHAT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SagePrimaryLight,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${studiedInChatList.size} topics tracked",
                        fontSize = 11.sp,
                        color = SageTextMuted
                    )
                }
            }

            if (studiedInChatList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SageSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SageCardBorder))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No chat topics studied yet",
                                color = SageTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start learning any roadmap topic in Sage Chat to build your study records.",
                                color = SageTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(studiedInChatList) { topic ->
                    StudiedTopicCard(
                        topic = topic,
                        onContinue = {
                            val node = roadmap?.nodes?.find { it.id == topic.nodeId }
                            if (node != null) {
                                onContinueLearning(node)
                            }
                        }
                    )
                }
            }

            // ==========================================
            // 3. NEXT UP RECOMMENDATION
            // ==========================================
            if (nextRecommendedNode != null) {
                item {
                    Text(
                        text = "NEXT UP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SageGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_roadmap_next_up_card"),
                        colors = CardDefaults.cardColors(containerColor = SageRaisedSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SageGold.copy(alpha = 0.5f)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "→",
                                        color = SageGold,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = nextRecommendedNode.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SageTextPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SageGold.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = nextRecommendedNode.category.uppercase(),
                                        color = SageGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Why next: ${nextRecommendedNode.description}",
                                color = SageTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onContinueLearning(nextRecommendedNode) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("chat_roadmap_study_next_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SagePrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Start Learning with Sage",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. STILL TO LEARN
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STILL TO LEARN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SagePrimaryLight,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$remainingCount topics remaining",
                        fontSize = 11.sp,
                        color = SageTextMuted
                    )
                }
            }

            val unlearnedPreview = stillToLearnNodes.take(8)
            items(unlearnedPreview) { node ->
                val status = statusMap[node.id] ?: "NOT_STARTED"
                val isCurrent = node.id == currentTopicNode?.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContinueLearning(node) }
                        .testTag("still_to_learn_item_${node.id}"),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isCurrent) SageGold.copy(alpha = 0.6f) else SageCardBorder
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isCurrent) "→" else "○",
                                color = if (isCurrent) SageGold else SageTextMuted,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column {
                                Text(
                                    text = node.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) SageTextPrimary else SageTextSecondary
                                )
                                Text(
                                    text = "${node.category} • ${node.difficulty ?: "Core"}",
                                    fontSize = 10.sp,
                                    color = SageTextMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Learn",
                            tint = SageTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (stillToLearnNodes.size > 8) {
                item {
                    Text(
                        text = "+ ${stillToLearnNodes.size - 8} more topics in full roadmap",
                        fontSize = 11.sp,
                        color = SageTextMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // ==========================================
            // 5. YOUR LEARNING PATH (COMPACT STEP-BY-STEP)
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "YOUR LEARNING PATH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SagePrimaryLight,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SageRaisedSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SageCardBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        roadmap?.nodes?.take(10)?.forEachIndexed { index, node ->
                            val status = statusMap[node.id] ?: "NOT_STARTED"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onContinueLearning(node) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (icon, iconColor) = when (status) {
                                    "COMPLETED" -> "✓" to SageSuccess
                                    "IN_PROGRESS" -> "→" to SageGold
                                    "NEEDS_REVIEW" -> "⚠️" to SageWarning
                                    else -> "○" to SageTextMuted
                                }
                                Text(
                                    text = icon,
                                    color = iconColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(
                                    text = node.title,
                                    fontSize = 13.sp,
                                    color = if (status == "COMPLETED") SageTextPrimary else SageTextSecondary,
                                    fontWeight = if (status == "IN_PROGRESS") FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 6. BOTTOM ACTIONS (SECTION 10 MANDATE)
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. [ Continue Learning ]
                    Button(
                        onClick = {
                            if (currentTopicNode != null) {
                                onContinueLearning(currentTopicNode)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("chat_roadmap_action_continue"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SagePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continue Learning (${currentTopicNode?.title ?: "Current Topic"})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // 2. [ View Full Roadmap ]
                    OutlinedButton(
                        onClick = { onViewFullRoadmap(roadmapId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("chat_roadmap_action_view_full"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SageTextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SagePrimaryLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = SagePrimaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Full Roadmap",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SageTextPrimary
                        )
                    }

                    // 3. [ Practice Weak Topics ]
                    Button(
                        onClick = onPracticeWeakTopics,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("chat_roadmap_action_practice_weak"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SageRaisedSurface,
                            contentColor = SageGold
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageGold.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = SageGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Practice Weak Topics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SageGold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StudiedTopicCard(
    topic: TopicProgressEntity,
    onContinue: () -> Unit
) {
    val statusColor = when (topic.status) {
        "COMPLETED" -> SageSuccess
        "IN_PROGRESS" -> SageGold
        "NEEDS_REVIEW" -> SageWarning
        else -> SagePrimaryLight
    }

    val statusLabel = when (topic.status) {
        "COMPLETED" -> "✓ Studied"
        "IN_PROGRESS" -> "⚡ In Progress"
        "NEEDS_REVIEW" -> "⚠️ Review"
        else -> "Studied"
    }

    val dateFormatted = remember(topic.lastStudiedAt) {
        val last = topic.lastStudiedAt ?: return@remember "Recently"
        val diffDays = (System.currentTimeMillis() - last) / (1000 * 60 * 60 * 24)
        when {
            diffDays < 1 -> "Today"
            diffDays == 1L -> "Yesterday"
            else -> "$diffDays days ago"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContinue() }
            .testTag("studied_topic_card_${topic.nodeId}"),
        colors = CardDefaults.cardColors(containerColor = SageSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SageCardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✓ ${topic.nodeTitle}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SageTextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${topic.sessionsCount.coerceAtLeast(1)} sessions",
                        fontSize = 11.sp,
                        color = SageTextSecondary
                    )
                    Text(text = "•", fontSize = 11.sp, color = SageTextMuted)
                    Text(
                        text = "Last: $dateFormatted",
                        fontSize = 11.sp,
                        color = SageTextMuted
                    )
                }

                if (topic.quizScore != null) {
                    Text(
                        text = "Quiz: ${topic.quizScore}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (topic.quizScore >= 70) SageSuccess else SageWarning
                    )
                }
            }
        }
    }
}
