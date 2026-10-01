package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyQuizRecordEntity
import com.example.data.local.TopicProgressEntity
import com.example.data.mission.DailyMissionEntity
import com.example.data.mission.MissionTask
import com.example.data.roadmap.DevRoadmapDetail
import com.example.data.roadmap.DevRoadmapNode
import com.example.data.studytools.AcademicContext
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.DailyMissionCard
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
    todayDailyMission: DailyMissionEntity? = null,
    academicProfile: com.example.data.profile.AcademicProfile? = null,
    onOpenAcademicProfileDialog: () -> Unit = {},
    onOpenRoadmap: (String) -> Unit,
    onContinueLearning: (DevRoadmapNode) -> Unit,
    onOpenDailyQuiz: () -> Unit,
    onStartNextTopic: (DevRoadmapNode) -> Unit,
    onExploreRoadmaps: () -> Unit,
    onStartDailyMission: (DailyMissionEntity) -> Unit = {},
    onStartFiveMinuteFocus: (DailyMissionEntity) -> Unit = {},
    onToggleDailyMissionTask: (taskId: String) -> Unit = {},
    onDailyMissionTaskAction: (task: MissionTask, context: AcademicContext) -> Unit = { _, _ -> },
    onReviewDailyMission: (DailyMissionEntity) -> Unit = {},
    isAiConnected: Boolean = true,
    onOpenDiagnostics: () -> Unit = {},
    onOpenStudyTools: () -> Unit = {},
    onOpenFocusMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val roadmapDetail = activeRoadmapDetail
    val roadmapTitle = roadmapDetail?.title ?: (academicProfile?.let { "${it.departmentName} - Semester ${it.semester}" } ?: "Official Academic Curriculum")
    val roadmapIcon = roadmapDetail?.icon ?: "🎓"
    val roadmapId = roadmapDetail?.id ?: ""

    val statusMap = remember(progressList) {
        progressList.associate { it.nodeId to it.status }
    }

    val totalNodes = roadmapDetail?.nodes?.size ?: 5
    val completedCount = progressList.count { it.status == "COMPLETED" }
    val percentComplete = if (totalNodes > 0) (completedCount * 100) / totalNodes else 0

    val animatedProgress by animateFloatAsState(
        targetValue = percentComplete / 100f,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "home_progress_bar"
    )

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

    AmbientGlowBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
                            .background(SageGlassL2)
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        SageGold.copy(alpha = 0.6f)
                                    )
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("home_streak_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔥", fontSize = 15.sp)
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

            // Academic Profile Setup Banner (First-time users) or Academic Context Badge
            if (academicProfile == null) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("academic_profile_setup_prompt_card"),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(18.dp),
                        glowColor = SagePrimaryLight.copy(alpha = 0.35f),
                        borderColor = SagePrimaryLight.copy(alpha = 0.5f),
                        onClick = onOpenAcademicProfileDialog
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(SagePrimaryStart, SagePrimary)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🎓", fontSize = 22.sp)
                                }
                                Column {
                                    Text(
                                        text = "Set Up Academic Profile",
                                        color = SageTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Choose Department & Semester to personalize syllabus & missions",
                                        color = SageTextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                            Button(
                                onClick = onOpenAcademicProfileDialog,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("setup_profile_button")
                            ) {
                                Text("Set Up", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                item {
                    // Active Academic Profile Badge with Change Action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SageGlassL1)
                            .border(1.dp, SageGlassBorder, RoundedCornerShape(14.dp))
                            .clickable { onOpenAcademicProfileDialog() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("home_active_academic_profile_chip")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎓", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${academicProfile.departmentName} • Sem ${academicProfile.semester}",
                                        color = SageTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${academicProfile.programmeName} (${academicProfile.regulation})",
                                        color = SageTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Change",
                                    color = SagePrimaryLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Change Academic Profile",
                                    tint = SagePrimaryLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!isAiConnected) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_offline_notice_card"),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(16.dp),
                        borderColor = SageGold.copy(alpha = 0.5f),
                        onClick = { onOpenDiagnostics() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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

            // Phase C2: Daily Mission Card
            if (todayDailyMission != null) {
                item {
                    DailyMissionCard(
                        mission = todayDailyMission,
                        onStartMission = onStartDailyMission,
                        onStartFiveMinuteFocus = onStartFiveMinuteFocus,
                        onToggleTask = onToggleDailyMissionTask,
                        onTaskAction = onDailyMissionTaskAction,
                        onReviewMission = onReviewDailyMission,
                        onContinueLearning = {
                            if (currentTopicNode != null) {
                                onContinueLearning(currentTopicNode)
                            } else {
                                onOpenFocusMode()
                            }
                        }
                    )
                }
            }

            // Focus Mode Feature Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_focus_mode_banner_card"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(18.dp),
                    glowColor = SageGold.copy(alpha = 0.2f),
                    borderColor = SageGold.copy(alpha = 0.35f),
                    onClick = onOpenFocusMode
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(SageGold.copy(alpha = 0.85f), SagePrimaryStart)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎯", fontSize = 22.sp)
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Focus Mode",
                                        color = SageTextPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SageGold.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "DISTRACTION-FREE",
                                            color = SageGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "Curriculum-grounded learning sessions with timer & checkpoints",
                                    color = SageTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = 2
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Focus Mode",
                            tint = SageGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Sage Study Tools Feature Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_study_tools_banner_card"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(18.dp),
                    glowColor = SagePrimaryLight.copy(alpha = 0.25f),
                    borderColor = SagePrimaryStart.copy(alpha = 0.4f),
                    onClick = onOpenStudyTools
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(SagePrimaryStart, SagePrimary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "⚡", fontSize = 22.sp)
                            }
                            Column {
                                Text(
                                    text = "Sage Study Tools",
                                    color = SageTextPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Scan & Solve • Notes • Flashcards • Mind Maps • Formula Sheets",
                                    color = SageTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = 2
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Study Tools",
                            tint = SagePrimaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Section 1: CURRENT ROADMAP CARD
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_current_roadmap_card"),
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
                            val isCurriculum = roadmapId.startsWith("curriculum_")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isCurriculum) "OFFICIAL CURRICULUM" else "CURRENT ROADMAP",
                                    color = if (isCurriculum) SageGold else SagePrimaryLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                if (isCurriculum) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SageSuccess.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SYLLABUS",
                                            color = SageSuccess,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SageGlassL1)
                                    .border(1.dp, SageSuccess.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
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
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(SageGlassL1)
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = roadmapIcon, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
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
                            progress = { animatedProgress },
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
                            GlassButton(
                                text = "View Roadmap",
                                onClick = { onOpenRoadmap(roadmapId) },
                                icon = Icons.Default.Map,
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("view_roadmap_button"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            GlassButton(
                                text = "Browse Paths",
                                onClick = onExploreRoadmaps,
                                variant = GlassButtonVariant.Secondary,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("switch_roadmap_button"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Section 2: CONTINUE LEARNING CARD
            if (currentTopicNode != null) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_continue_learning_card"),
                        level = GlassLevel.L3,
                        glowColor = SagePrimaryLight,
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
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
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    SagePrimaryStart.copy(alpha = 0.5f),
                                                    SagePrimary.copy(alpha = 0.3f)
                                                )
                                            )
                                        )
                                        .border(1.dp, SageGlassBorderGlow, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = currentTopicNode.icon, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

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

                            GlassButton(
                                text = "Continue Learning with Sage AI",
                                onClick = { onContinueLearning(currentTopicNode) },
                                icon = Icons.Default.PlayArrow,
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("continue_learning_button"),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }

            // Section 3: TODAY'S QUIZ CARD
            item {
                val isQuizDone = todayDailyQuiz?.isCompleted == true

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_today_quiz_card"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isQuizDone) SageSuccess.copy(alpha = 0.16f) else SageGold.copy(alpha = 0.16f))
                                    .border(
                                        1.dp,
                                        if (isQuizDone) SageSuccess.copy(alpha = 0.4f) else SageGold.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
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
                            lineHeight = 19.sp
                        )

                        GlassButton(
                            text = if (isQuizDone) "View Daily Quiz Dashboard" else "Take Daily Quiz",
                            onClick = onOpenDailyQuiz,
                            icon = Icons.Default.Quiz,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("take_daily_quiz_button"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Section 4: RECOMMENDED NEXT TOPIC
            if (recommendedNextNode != null) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_recommended_next_card"),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(SageGlassL1)
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = recommendedNextNode.icon, fontSize = 22.sp)
                                }
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

                            GlassButton(
                                text = "Start ${recommendedNextNode.title}",
                                onClick = { onStartNextTopic(recommendedNextNode) },
                                icon = Icons.Default.ArrowForward,
                                variant = GlassButtonVariant.Secondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("start_recommended_topic_button"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "This app is built by Piyush Kumar",
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "CSE (AI/ML)",
                        color = SageTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
