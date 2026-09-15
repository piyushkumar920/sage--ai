package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyQuizRecordEntity
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

@Composable
fun HomeScreen(
    streakDays: Int,
    activeRoadmapDetail: DevRoadmapDetail?,
    progressList: List<TopicProgressEntity>,
    todayDailyQuiz: DailyQuizRecordEntity?,
    onOpenRoadmap: (String) -> Unit,
    onContinueLearning: (DevRoadmapNode) -> Unit,
    onOpenDailyQuiz: () -> Unit,
    onStartNextTopic: (DevRoadmapNode) -> Unit,
    onExploreRoadmaps: () -> Unit,
    isAiConnected: Boolean = true,
    onOpenDiagnostics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val roadmapDetail = activeRoadmapDetail
    val roadmapTitle = roadmapDetail?.title ?: "Full Stack Developer"
    val roadmapIcon = roadmapDetail?.icon ?: "🌐"
    val roadmapId = roadmapDetail?.id ?: "fullstack"

    val statusMap = remember(progressList) {
        progressList.associate { it.nodeId to it.status }
    }

    val totalNodes = roadmapDetail?.nodes?.size ?: 50
    val completedCount = progressList.count { it.status == "COMPLETED" }
    val percentComplete = if (totalNodes > 0) (completedCount * 100) / totalNodes else 42

    // Current Topic (in progress or first uncompleted)
    val currentTopicNode = remember(roadmapDetail, progressList) {
        roadmapDetail?.nodes?.find { statusMap[it.id] == "IN_PROGRESS" }
            ?: roadmapDetail?.nodes?.find { statusMap[it.id] == "NEEDS_REVIEW" }
            ?: roadmapDetail?.nodes?.find { statusMap[it.id] != "COMPLETED" }
            ?: roadmapDetail?.nodes?.firstOrNull()
    }

    // Recommended Next Topic (uncompleted after current)
    val recommendedNextNode = remember(roadmapDetail, progressList, currentTopicNode) {
        roadmapDetail?.nodes?.find { node ->
            node.id != currentTopicNode?.id && (statusMap[node.id] == null || statusMap[node.id] == "NOT_STARTED")
        } ?: roadmapDetail?.nodes?.getOrNull(1)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SageBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Streak
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WELCOME BACK",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Sage AI Learning",
                        color = SageTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // 🔥 Current Streak Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SageSurface)
                        .border(1.dp, SageGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("home_streak_badge")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔥", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$streakDays Day Streak",
                            color = SageGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (!isAiConnected) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenDiagnostics() }
                        .testTag("ai_offline_notice_card"),
                    colors = CardDefaults.cardColors(containerColor = SageRaisedSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SageGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Learning Mode Active",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageTextPrimary
                            )
                            Text(
                                text = "Roadmaps, flashcards & quizzes are ready. Tap to connect AI.",
                                fontSize = 11.sp,
                                color = SageTextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SageGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Section 1: CURRENT ROADMAP CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_current_roadmap_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT ROADMAP",
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SageRaisedSurface)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$percentComplete% Complete",
                                color = SageSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = roadmapIcon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = roadmapTitle,
                                color = SageTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedCount of $totalNodes topics completed",
                                color = SageTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { percentComplete / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SagePrimary,
                        trackColor = SageRaisedSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onOpenRoadmap(roadmapId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("view_roadmap_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Roadmap", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onExploreRoadmaps,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("switch_roadmap_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = SageSurface)
                        ) {
                            Text("Browse Paths", color = SageTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Section 2: CONTINUE LEARNING CARD
        if (currentTopicNode != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_continue_learning_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SagePrimaryLight.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONTINUE LEARNING",
                                color = SageGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentTopicNode.category.uppercase(),
                                color = SageTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SageRaisedSurface)
                                    .border(1.dp, SageCardBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = currentTopicNode.icon, fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentTopicNode.title,
                                    color = SageTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentTopicNode.description.ifBlank { "Core module in $roadmapTitle" },
                                    color = SageTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = { onContinueLearning(currentTopicNode) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("continue_learning_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Continue Learning with Sage AI", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 3: TODAY'S QUIZ CARD
        item {
            val isQuizDone = todayDailyQuiz?.isCompleted == true

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_today_quiz_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY'S QUIZ",
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isQuizDone) SageSuccess.copy(alpha = 0.15f) else SageGold.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isQuizDone) "Completed ✓" else "Ready Today",
                                color = if (isQuizDone) SageSuccess else SageGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = todayDailyQuiz?.question ?: "Test your mastery on recent curriculum topics and weak concepts.",
                        color = SageTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = onOpenDailyQuiz,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("take_daily_quiz_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SageRaisedSurface)
                    ) {
                        Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = SagePrimaryLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isQuizDone) "View Daily Quiz Dashboard" else "Take Daily Quiz",
                            color = SagePrimaryLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 4: RECOMMENDED NEXT TOPIC
        if (recommendedNextNode != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_recommended_next_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECOMMENDED NEXT",
                                color = SageTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Prerequisite Ready",
                                color = SageSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = recommendedNextNode.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recommendedNextNode.title,
                                    color = SageTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = recommendedNextNode.description.ifBlank { "Subsequent concept in $roadmapTitle" },
                                    color = SageTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { onStartNextTopic(recommendedNextNode) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("start_recommended_topic_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SagePrimaryLight),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = SageSurface)
                        ) {
                            Text("Start ${recommendedNextNode.title}", color = SagePrimaryLight, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = SagePrimaryLight, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
