package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TopicProgressEntity
import com.example.data.local.WeakConceptEntity
import com.example.data.focus.FocusStats
import com.example.data.mission.DailyMissionEntity
import com.example.data.adaptive.AdaptiveWeakConceptItem
import com.example.data.adaptive.WeeklyReviewData
import com.example.data.roadmap.DevRoadmapDetail
import com.example.data.roadmap.DevRoadmapNode
import com.example.data.roadmap.DevRoadmapSummary
import com.example.data.studytools.AcademicContext
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.DailyMissionsHistorySection
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.screens.pyq.PYQScreenSection
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
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageWarning

/**
 * Progress tabs: Academics, Careers, and PYQ.
 */
enum class ProgressTab(val title: String, val icon: String) {
    ACADEMICS("Academics", "🎓"),
    CAREERS("Careers", "💼"),
    PYQ("PYQ", "📚")
}

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
    academicContext: AcademicContext? = null,
    careerSummaries: List<DevRoadmapSummary> = emptyList(),
    roadmapPercentages: Map<String, Int> = emptyMap(),
    focusStats: FocusStats = FocusStats(),
    dailyMissions: List<DailyMissionEntity> = emptyList(),
    weeklyReviewData: WeeklyReviewData? = null,
    onStartReviewSession: (concept: String, context: AcademicContext?) -> Unit = { _, _ -> },
    onReviewWeakConcept: (concept: String, context: AcademicContext?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(ProgressTab.ACADEMICS) }

    AmbientGlowBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation Segment Bar: Academics | Careers | PYQ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SageGlassL2)
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    SageGlassBorderGlow.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProgressTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SagePrimaryStart.copy(alpha = 0.45f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) SagePrimaryLight.copy(alpha = 0.5f) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { currentTab = tab }
                                .padding(vertical = 8.dp)
                                .testTag("progress_tab_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = tab.icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color.White else SageTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Tab Content
            when (currentTab) {
                ProgressTab.ACADEMICS -> {
                    AcademicsProgressContent(
                        activeRoadmapDetail = activeRoadmapDetail,
                        progressList = progressList,
                        streakDays = streakDays,
                        longestStreak = longestStreak,
                        quizAverage = quizAverage,
                        quizzesCompleted = quizzesCompleted,
                        recentlyCompleted = recentlyCompleted,
                        weakConcepts = weakConcepts,
                        focusStats = focusStats,
                        dailyMissions = dailyMissions,
                        weeklyReviewData = weeklyReviewData,
                        onSelectTopic = onSelectTopic,
                        onPracticeWeakConcept = onPracticeWeakConcept,
                        onReviewWeakConcept = onReviewWeakConcept,
                        onStartReviewSession = onStartReviewSession,
                        onOpenRoadmap = onOpenRoadmap
                    )
                }

                ProgressTab.CAREERS -> {
                    CareersProgressSection(
                        careerSummaries = careerSummaries,
                        roadmapPercentages = roadmapPercentages,
                        onOpenRoadmap = onOpenRoadmap
                    )
                }

                ProgressTab.PYQ -> {
                    PYQScreenSection(
                        academicContext = academicContext,
                        fallbackCourseTitle = activeRoadmapDetail?.title
                    )
                }
            }
        }
    }
}

@Composable
private fun AcademicsProgressContent(
    activeRoadmapDetail: DevRoadmapDetail?,
    progressList: List<TopicProgressEntity>,
    streakDays: Int,
    longestStreak: Int,
    quizAverage: Double?,
    quizzesCompleted: Int,
    recentlyCompleted: List<TopicProgressEntity>,
    weakConcepts: List<WeakConceptEntity>,
    focusStats: FocusStats = FocusStats(),
    dailyMissions: List<DailyMissionEntity> = emptyList(),
    weeklyReviewData: WeeklyReviewData? = null,
    onSelectTopic: (String) -> Unit,
    onPracticeWeakConcept: (concept: String, topicContext: String) -> Unit,
    onReviewWeakConcept: (concept: String, context: AcademicContext?) -> Unit = { _, _ -> },
    onStartReviewSession: (concept: String, context: AcademicContext?) -> Unit = { _, _ -> },
    onOpenRoadmap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val roadmapTitle = activeRoadmapDetail?.title ?: "CS101: Introduction to Programming and Problem Solving"
    val roadmapId = activeRoadmapDetail?.id ?: "curriculum_cse_aiml_CS101"
    val totalTopics = activeRoadmapDetail?.nodes?.size ?: 5

    val completedCount = progressList.count { it.status == "COMPLETED" }
    val inProgressCount = progressList.count { it.status == "IN_PROGRESS" }
    val needsReviewCount = progressList.count { it.status == "NEEDS_REVIEW" }
    val remainingCount = (totalTopics - completedCount).coerceAtLeast(0)
    val percentComplete = if (totalTopics > 0) (completedCount * 100) / totalTopics else 0

    val animatedProgress by animateFloatAsState(
        targetValue = percentComplete / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress_analytics_bar"
    )

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
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SageGlassL2)
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    SageGlassBorderGlow.copy(alpha = 0.4f)
                                )
                            ),
                            CircleShape
                        )
                        .testTag("progress_open_roadmap_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "View Roadmap",
                        tint = SagePrimaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Section 1: OVERALL ROADMAP COMPLETION CARD
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progress_overall_card"),
                level = GlassLevel.L2,
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        )
                        Text(
                            text = "$percentComplete% Complete",
                            color = SagePrimaryLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = SagePrimary,
                        trackColor = SageGlassL1
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
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("progress_stat_current_streak"),
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(18.dp)
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
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("progress_stat_longest_streak"),
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(18.dp)
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
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("progress_stat_quiz_average"),
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(18.dp)
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
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("progress_stat_quizzes_completed"),
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(18.dp)
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

        // Section 2.5: FOCUS ACTIVITY CARD
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progress_focus_activity_card"),
                level = GlassLevel.L2,
                shape = RoundedCornerShape(20.dp),
                borderColor = SageGold.copy(alpha = 0.35f)
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
                            Text(text = "🎯", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FOCUS ACTIVITY",
                                color = SageGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Learning Sessions Only",
                            color = SageTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Today's Focus
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SageGlassL1)
                                .border(1.dp, SageGlassBorder, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Today's Focus",
                                    color = SageTextSecondary,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${focusStats.todayMinutes} min",
                                    color = SageTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // This Week
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SageGlassL1)
                                .border(1.dp, SageGlassBorder, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "This Week",
                                    color = SageTextSecondary,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = focusStats.thisWeekFormatted,
                                    color = SageTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Total Sessions
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SageGlassL1)
                                .border(1.dp, SageGlassBorder, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Sessions",
                                    color = SageTextSecondary,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${focusStats.totalSessionsCount}",
                                    color = SageGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2.5: DAILY MISSIONS HISTORY
        item {
            DailyMissionsHistorySection(missions = dailyMissions)
        }

        // Section 3: CURRENT LEARNING TOPIC
        if (currentLearningTopic != null) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTopic(currentLearningTopic.id) }
                        .testTag("progress_current_topic_card"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
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
                            Text(text = currentLearningTopic.icon, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CURRENT LEARNING TOPIC",
                                color = SagePrimaryLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentLearningTopic.title,
                                color = SageTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SageTextSecondary)
                    }
                }
            }
        }

        // Section 4: RECOMMENDED NEXT TOPIC
        if (recommendedNextTopic != null) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTopic(recommendedNextTopic.id) }
                        .testTag("progress_recommended_topic_card"),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SageGlassL2)
                                .border(1.dp, SageGlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = recommendedNextTopic.icon, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RECOMMENDED NEXT TOPIC",
                                color = SageTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = recommendedNextTopic.title,
                                color = SageTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SageTextSecondary)
                    }
                }
            }
        }

        // Section 4.5: WEEKLY REVIEW (PHASE C3)
        if (weeklyReviewData != null) {
            item {
                Text(
                    text = "WEEKLY REVIEW",
                    color = SagePrimaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_review_card"),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "THIS WEEK'S ACTIVITY",
                                color = SagePrimaryLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SagePrimaryStart.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Adaptive Learning",
                                    color = SagePrimaryLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5 Activity Metrics in Clean Responsive Row/Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Focus Time
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = weeklyReviewData.totalFocusMinutesFormatted,
                                    color = SageTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Focus Time",
                                    color = SageTextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            // Focus Sessions
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${weeklyReviewData.focusSessionsCompleted}",
                                    color = SageTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sessions",
                                    color = SageTextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            // Daily Missions
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${weeklyReviewData.dailyMissionsCompleted}",
                                    color = SageSuccess,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Missions",
                                    color = SageTextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            // Topics Studied
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${weeklyReviewData.topicsStudiedCount}",
                                    color = SagePrimaryLight,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Topics",
                                    color = SageTextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            // PYQs Attempted
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${weeklyReviewData.pyqsAttemptedCount}",
                                    color = SageGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "PYQs",
                                    color = SageTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // What You Learned This Week
                        val learnedList: List<String> = weeklyReviewData.learnedTopicsThisWeek
                        if (learnedList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "WHAT YOU LEARNED",
                                color = SageTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (topicName in learnedList.take(3)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SageSuccess.copy(alpha = 0.12f))
                                            .border(1.dp, SageSuccess.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "✓ $topicName",
                                            color = SageSuccess,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // What to Review Next
                        val topReviewConcept: String = weeklyReviewData.reviewNextConcept ?: weeklyReviewData.weakConcepts.firstOrNull()?.concept ?: ""
                        if (topReviewConcept.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SageWarning.copy(alpha = 0.10f))
                                    .border(1.dp, SageWarning.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "RECOMMENDED FOR REVIEW",
                                            color = SageWarning,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SageWarning.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Active Target",
                                                color = SageWarning,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = topReviewConcept,
                                        color = SageTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    GlassButton(
                                        text = "Start 20m Review Session",
                                        onClick = {
                                            onStartReviewSession(
                                                topReviewConcept,
                                                AcademicContext(
                                                    department = "",
                                                    programme = "",
                                                    regulation = "",
                                                    semester = 1,
                                                    courseCode = "",
                                                    courseName = roadmapTitle,
                                                    module = "",
                                                    topic = topReviewConcept
                                                )
                                            )
                                        },
                                        variant = GlassButtonVariant.Primary,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 5: WEAK CONCEPTS & REMEDIATION (Targeted Practice Drill)
        val adaptiveWeakList: List<AdaptiveWeakConceptItem> = weeklyReviewData?.weakConcepts ?: emptyList()
        val totalWeakCount = if (adaptiveWeakList.isNotEmpty()) adaptiveWeakList.size else weakConcepts.size
        item {
            Text(
                text = "WEAK CONCEPTS ($totalWeakCount)",
                color = SageWarning,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (adaptiveWeakList.isEmpty() && weakConcepts.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "No weak concepts identified! Keep maintaining your streak and quiz accuracy.",
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else if (adaptiveWeakList.isNotEmpty()) {
            items(
                items = adaptiveWeakList,
                key = { it.id }
            ) { weakItem: AdaptiveWeakConceptItem ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weak_concept_${weakItem.id}"),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = weakItem.concept,
                                color = SageTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Supportive status badge
                            val badgeColor = when (weakItem.statusBadge) {
                                "Needs Review" -> SageWarning
                                "Needs Practice" -> SageError
                                else -> SagePrimaryLight
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = weakItem.statusBadge,
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (weakItem.courseName.isNotBlank() || weakItem.reason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (weakItem.reason.isNotBlank()) weakItem.reason else "Course: ${weakItem.courseName}",
                                color = SageTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassButton(
                                text = "Review with Sage",
                                onClick = {
                                    val courseTitle = if (weakItem.courseName.isNotBlank()) weakItem.courseName else roadmapTitle
                                    onReviewWeakConcept(
                                        weakItem.concept,
                                        AcademicContext(
                                            department = "",
                                            programme = "",
                                            regulation = "",
                                            semester = 1,
                                            courseCode = "",
                                            courseName = courseTitle,
                                            module = "",
                                            topic = weakItem.concept
                                        )
                                    )
                                },
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier.weight(1f)
                            )
                            GlassButton(
                                text = "Practice",
                                onClick = {
                                    val courseTitle = if (weakItem.courseName.isNotBlank()) weakItem.courseName else roadmapTitle
                                    onPracticeWeakConcept(weakItem.concept, courseTitle)
                                },
                                variant = GlassButtonVariant.Secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        } else {
            items(weakConcepts) { weak ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weak_concept_${weak.id}"),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = weak.concept,
                                color = SageTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${weak.mistakeCount} quiz misses • Needs remediation",
                                color = SageError,
                                fontSize = 11.sp
                            )
                        }

                        GlassButton(
                            text = "Practice",
                            onClick = { onPracticeWeakConcept(weak.concept, roadmapTitle) },
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .testTag("practice_button_${weak.id}")
                        )
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
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Complete your first topic quiz to populate your verification record.",
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentlyCompleted) { topic ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTopic(topic.nodeId) },
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(SageSuccess.copy(alpha = 0.15f))
                                .border(1.dp, SageSuccess.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", color = SageSuccess, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.nodeTitle,
                                color = SageTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${topic.category} • Score: ${topic.quizScore ?: 100}%", color = SageTextSecondary, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SageTextSecondary)
                    }
                }
            }
        }

        // Safe bottom spacing
        item {
            Spacer(modifier = Modifier.height(96.dp))
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
