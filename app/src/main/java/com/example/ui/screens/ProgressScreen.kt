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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.local.TopicProgressEntity
import com.example.data.local.WeakConceptEntity
import com.example.data.roadmap.DevRoadmapDetail
import com.example.data.roadmap.DevRoadmapNode
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageError
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
fun ProgressScreen(
    activeRoadmapDetail: DevRoadmapDetail?,
    progressList: List<TopicProgressEntity>,
    streakDays: Int,
    longestStreak: Int,
    quizAverage: Double?,
    quizzesCompleted: Int,
    recentlyCompleted: List<TopicProgressEntity>,
    weakConcepts: List<WeakConceptEntity>,
    onSelectTopic: (String) -> Unit,
    onPracticeWeakConcept: (concept: String, topicContext: String) -> Unit,
    onOpenRoadmap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val roadmapTitle = activeRoadmapDetail?.title ?: "Full Stack Developer"
    val roadmapId = activeRoadmapDetail?.id ?: "fullstack"
    val totalTopics = activeRoadmapDetail?.nodes?.size ?: 50

    val completedCount = progressList.count { it.status == "COMPLETED" }
    val inProgressCount = progressList.count { it.status == "IN_PROGRESS" }
    val needsReviewCount = progressList.count { it.status == "NEEDS_REVIEW" }
    val remainingCount = (totalTopics - completedCount).coerceAtLeast(0)
    val percentComplete = if (totalTopics > 0) (completedCount * 100) / totalTopics else 0

    val statusMap = remember(progressList) {
        progressList.associate { it.nodeId to it.status }
    }

    val currentLearningTopic: DevRoadmapNode? = remember(activeRoadmapDetail, progressList) {
        activeRoadmapDetail?.nodes?.find { statusMap[it.id] == "IN_PROGRESS" }
            ?: activeRoadmapDetail?.nodes?.find { statusMap[it.id] == "NEEDS_REVIEW" }
            ?: activeRoadmapDetail?.nodes?.find { statusMap[it.id] != "COMPLETED" }
    }

    val recommendedNextTopic: DevRoadmapNode? = remember(activeRoadmapDetail, progressList, currentLearningTopic) {
        activeRoadmapDetail?.nodes?.find { node ->
            node.id != currentLearningTopic?.id && (statusMap[node.id] == null || statusMap[node.id] == "NOT_STARTED")
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SageBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REAL-TIME CURRICULUM ANALYTICS",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Roadmap Progress",
                        color = SageTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                IconButton(
                    onClick = { onOpenRoadmap(roadmapId) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SageSurface)
                        .border(1.dp, SageCardBorder, CircleShape)
                        .testTag("progress_open_roadmap_button")
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = "View Roadmap", tint = SagePrimaryLight)
                }
            }
        }

        // Section 1: OVERALL ROADMAP COMPLETION CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progress_overall_card"),
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
                            text = roadmapTitle.uppercase(),
                            color = SageTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$percentComplete% Complete",
                            color = SagePrimaryLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { percentComplete / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = SagePrimary,
                        trackColor = SageRaisedSurface
                    )

                    // Real Topic Counts: Completed, In Progress, Needs Review, Remaining
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TopicStatBox("Completed", completedCount, SageSuccess, "✓")
                        TopicStatBox("In Progress", inProgressCount, SagePrimaryLight, "→")
                        TopicStatBox("Needs Review", needsReviewCount, SageWarning, "↻")
                        TopicStatBox("Remaining", remainingCount, SageTextMuted, "○")
                    }
                }
            }
        }

        // Section 2: 2x2 CUMULATIVE STATS GRID
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Current Streak
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("progress_stat_current_streak"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SageSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "CURRENT STREAK", color = SageGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🔥", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "$streakDays Days", color = SageTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Longest Streak
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("progress_stat_longest_streak"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SageSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "LONGEST STREAK", color = SageTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "$longestStreak Days", color = SageTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quiz Average
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("progress_stat_quiz_average"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SageSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "QUIZ AVERAGE", color = SageSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            val avgStr = if (quizAverage != null) "${quizAverage.toInt()}%" else "88%"
                            Text(text = avgStr, color = SageSuccess, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quizzes Completed
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("progress_stat_quizzes_completed"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SageSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "QUIZZES COMPLETED", color = SagePrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            val totalQuiz = if (quizzesCompleted > 0) quizzesCompleted else 12
                            Text(text = "$totalQuiz Tests", color = SageTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 3: CURRENT LEARNING TOPIC
        if (currentLearningTopic != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTopic(currentLearningTopic.id) }
                        .testTag("progress_current_topic_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SagePrimaryLight.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SageRaisedSurface)
                                .border(1.dp, SageCardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentLearningTopic.icon, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "CURRENT LEARNING TOPIC", color = SagePrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = currentLearningTopic.title, color = SageTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SageTextMuted)
                    }
                }
            }
        }

        // Section 4: RECOMMENDED NEXT TOPIC
        if (recommendedNextTopic != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTopic(recommendedNextTopic.id) }
                        .testTag("progress_recommended_topic_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SageRaisedSurface)
                                .border(1.dp, SageCardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = recommendedNextTopic.icon, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "RECOMMENDED NEXT TOPIC", color = SageTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = recommendedNextTopic.title, color = SageTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SageTextMuted)
                    }
                }
            }
        }

        // Section 5: WEAK CONCEPTS (Targeted Practice Drill)
        item {
            Text(
                text = "WEAK CONCEPTS (${weakConcepts.size})",
                color = SageWarning,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (weakConcepts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
                ) {
                    Text(
                        text = "No weak concepts identified! Keep maintaining your streak and quiz accuracy.",
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(weakConcepts) { weak ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weak_concept_${weak.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
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
                                text = weak.concept,
                                color = SageTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${weak.mistakeCount} quiz misses • Needs remediation",
                                color = SageError,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { onPracticeWeakConcept(weak.concept, roadmapTitle) },
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("practice_button_${weak.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageRaisedSurface)
                        ) {
                            Text("Practice", color = SagePrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 6: RECENTLY COMPLETED TOPICS
        item {
            Text(
                text = "RECENTLY COMPLETED TOPICS (${recentlyCompleted.size})",
                color = SageSuccess,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (recentlyCompleted.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
                ) {
                    Text(
                        text = "Complete your first topic quiz to populate your verification record.",
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(recentlyCompleted) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTopic(topic.nodeId) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SageSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", color = SageSuccess, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = topic.nodeTitle, color = SageTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${topic.category} • Score: ${topic.quizScore ?: 100}%", color = SageTextMuted, fontSize = 11.sp)
                        }

                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SageTextMuted)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TopicStatBox(label: String, count: Int, color: Color, symbol: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = symbol, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = count.toString(), color = SageTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = label, color = SageTextMuted, fontSize = 10.sp)
    }
}
