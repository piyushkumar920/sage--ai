package com.example.ui.screens.focus

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.curriculum.CurriculumCourse
import com.example.data.curriculum.CurriculumModule
import com.example.data.curriculum.CurriculumRepository
import com.example.data.focus.FocusRepository
import com.example.data.local.TopicEntity
import com.example.data.studytools.AcademicContext
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.screens.CurriculumTopicSelectorDialog
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
import com.example.data.profile.AcademicProfile
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    focusViewModel: FocusViewModel,
    curriculumRepository: CurriculumRepository,
    allTopics: List<TopicEntity>,
    activeTopicId: Long? = null,
    currentAcademicProfile: AcademicProfile? = null,
    onOpenChatWithPrompt: (prompt: String, context: AcademicContext?) -> Unit,
    onOpenStudyChat: (context: AcademicContext?) -> Unit,
    onOpenScanAndSolve: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenState by focusViewModel.screenState.collectAsState()
    val academicContext by focusViewModel.academicContext.collectAsState()
    val plannedMinutes by focusViewModel.plannedMinutes.collectAsState()
    val goal by focusViewModel.goal.collectAsState()
    val remainingSeconds by focusViewModel.remainingSeconds.collectAsState()
    val isTimerRunning by focusViewModel.isTimerRunning.collectAsState()
    val actualFocusedSeconds by focusViewModel.actualFocusedSeconds.collectAsState()
    val learnCompleted by focusViewModel.learnCompleted.collectAsState()
    val practiceCompleted by focusViewModel.practiceCompleted.collectAsState()
    val reviewCompleted by focusViewModel.reviewCompleted.collectAsState()

    var showTopicSelectorDialog by remember { mutableStateOf(false) }
    var showStuckBottomSheet by remember { mutableStateOf(false) }

    BackHandler {
        if (screenState == FocusScreenState.ACTIVE) {
            // Keep session running/active, just exit full screen or confirm
            onBack()
        } else {
            onBack()
        }
    }

    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        when (screenState) {
            FocusScreenState.SETUP -> {
                FocusSetupView(
                    academicContext = academicContext,
                    plannedMinutes = plannedMinutes,
                    goal = goal,
                    onDurationSelected = { focusViewModel.setPlannedMinutes(it) },
                    onGoalChanged = { focusViewModel.setGoal(it) },
                    onChangeTopic = { showTopicSelectorDialog = true },
                    onStartFocus = { focusViewModel.startFocusSession() },
                    onBack = onBack
                )
            }

            FocusScreenState.ACTIVE -> {
                ActiveFocusView(
                    academicContext = academicContext,
                    remainingSeconds = remainingSeconds,
                    totalSeconds = plannedMinutes * 60,
                    isTimerRunning = isTimerRunning,
                    goal = goal,
                    learnCompleted = learnCompleted,
                    practiceCompleted = practiceCompleted,
                    reviewCompleted = reviewCompleted,
                    onTogglePlayPause = { focusViewModel.togglePlayPause() },
                    onToggleLearn = { focusViewModel.toggleLearnCheckpoint() },
                    onTogglePractice = { focusViewModel.togglePracticeCheckpoint() },
                    onToggleReview = { focusViewModel.toggleReviewCheckpoint() },
                    onStuck = { showStuckBottomSheet = true },
                    onOpenStudyChat = {
                        onOpenStudyChat(academicContext)
                    },
                    onFinishSession = { focusViewModel.finishSession(completedNormally = false) },
                    onBack = onBack
                )
            }

            FocusScreenState.SUMMARY -> {
                FocusSummaryView(
                    academicContext = academicContext,
                    plannedMinutes = plannedMinutes,
                    actualSeconds = actualFocusedSeconds,
                    goal = goal,
                    learnCompleted = learnCompleted,
                    practiceCompleted = practiceCompleted,
                    reviewCompleted = reviewCompleted,
                    onReviewWeakArea = {
                        val topic = academicContext?.topic ?: "Curriculum Concept"
                        val prompt = "Hi Sage! I just completed my Focus study session on '$topic'. Can you give me a diagnostic review question on the key weak spots students usually face in this topic?"
                        onOpenChatWithPrompt(prompt, academicContext)
                    },
                    onFinish = {
                        focusViewModel.resetToSetup()
                        onBack()
                    }
                )
            }
        }
    }

    // Dedicated Full-Screen Change Study Topic Sheet (Phase C3)
    if (showTopicSelectorDialog) {
        ChangeStudyTopicSheet(
            curriculumRepository = curriculumRepository,
            currentAcademicProfile = currentAcademicProfile,
            activeAcademicContext = academicContext,
            onSelectTopicContext = { newContext ->
                focusViewModel.setAcademicContext(newContext)
                showTopicSelectorDialog = false
            },
            onDismiss = { showTopicSelectorDialog = false }
        )
    }

    // I'm Stuck Bottom Sheet
    if (showStuckBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStuckBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SageBackground,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 4.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SageGlassBorder)
                )
            }
        ) {
            ImStuckBottomSheetContent(
                topic = academicContext?.topic ?: "Current Topic",
                onActionSelected = { action ->
                    showStuckBottomSheet = false
                    if (action == StuckAction.SCAN_QUESTION) {
                        onOpenScanAndSolve()
                    } else {
                        val prompt = focusViewModel.getStuckPrompt(action)
                        onOpenChatWithPrompt(prompt, academicContext)
                    }
                },
                onDismiss = { showStuckBottomSheet = false }
            )
        }
    }
}

/**
 * 1. Focus Setup View
 */
@Composable
private fun FocusSetupView(
    academicContext: AcademicContext?,
    plannedMinutes: Int,
    goal: String,
    onDurationSelected: (Int) -> Unit,
    onGoalChanged: (String) -> Unit,
    onChangeTopic: () -> Unit,
    onStartFocus: () -> Unit,
    onBack: () -> Unit
) {
    val durationPresets = listOf(5, 15, 25, 45, 60)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header with Back
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SageGlassL2)
                        .border(1.dp, SageGlassBorder, CircleShape)
                        .testTag("focus_setup_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SageTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "CURRICULUM LEARNING SESSION",
                        color = SagePrimaryLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Focus Session",
                        color = SageTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Academic Context Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_context_card"),
                level = GlassLevel.L2,
                shape = RoundedCornerShape(22.dp)
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
                            text = "OFFICIAL CURRICULUM CONTEXT",
                            color = SageGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Semester ${academicContext?.semester ?: 3}",
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${academicContext?.courseCode ?: "CS301"}: ${academicContext?.courseName ?: "Operating Systems"}",
                        color = SageTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (!academicContext?.module.isNullOrBlank()) {
                        Text(
                            text = academicContext?.module ?: "",
                            color = SageTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SageGlassL1)
                            .border(1.dp, SageGlassBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎯", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "CURRENT TOPIC",
                                    color = SagePrimaryLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = academicContext?.topic ?: "Process Scheduling",
                                    color = SageTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onChangeTopic,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("focus_change_topic_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SageGlassL1,
                            contentColor = SagePrimaryLight
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageGlassBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = SagePrimaryLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Change Topic", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Today's Goal Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_goal_card"),
                level = GlassLevel.L2,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TODAY'S GOAL",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = goal,
                        onValueChange = onGoalChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("focus_goal_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SagePrimary,
                            unfocusedBorderColor = SageGlassBorder,
                            focusedTextColor = SageTextPrimary,
                            unfocusedTextColor = SageTextPrimary,
                            focusedContainerColor = SageGlassL1,
                            unfocusedContainerColor = SageGlassL1
                        ),
                        placeholder = {
                            Text(text = "What will you accomplish in this session?", color = SageTextMuted, fontSize = 13.sp)
                        },
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }
        }

        // Duration Selection
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_duration_card"),
                level = GlassLevel.L2,
                shape = RoundedCornerShape(20.dp)
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
                            text = "SESSION DURATION",
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$plannedMinutes minutes",
                            color = SageGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durationPresets.forEach { mins ->
                            val isSelected = mins == plannedMinutes
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SagePrimaryStart else SageGlassL1)
                                    .border(
                                        1.dp,
                                        if (isSelected) SagePrimaryLight else SageGlassBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onDurationSelected(mins) }
                                    .padding(vertical = 10.dp)
                                    .testTag("focus_duration_${mins}min"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    color = if (isSelected) Color.White else SageTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Start Focus Button
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onStartFocus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_focus_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SagePrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = "🎯 Start Focus ($plannedMinutes min)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * 2. Active Focus View (Distraction-Minimized)
 */
@Composable
private fun ActiveFocusView(
    academicContext: AcademicContext?,
    remainingSeconds: Int,
    totalSeconds: Int,
    isTimerRunning: Boolean,
    goal: String,
    learnCompleted: Boolean,
    practiceCompleted: Boolean,
    reviewCompleted: Boolean,
    onTogglePlayPause: () -> Unit,
    onToggleLearn: () -> Unit,
    onTogglePractice: () -> Unit,
    onToggleReview: () -> Unit,
    onStuck: () -> Unit,
    onOpenStudyChat: () -> Unit,
    onFinishSession: () -> Unit,
    onBack: () -> Unit
) {
    val progressFraction = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds).coerceIn(0f, 1f)
    } else 0f

    val formattedTime = FocusRepository.formatTimerSeconds(remainingSeconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Minimal Top Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SageGlassL1)
                        .border(1.dp, SageGlassBorder, CircleShape)
                        .testTag("focus_active_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Minimize Focus",
                        tint = SageTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SageGold.copy(alpha = 0.15f))
                        .border(1.dp, SageGold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isTimerRunning) SageSuccess else SageWarning)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTimerRunning) "FOCUS MODE ACTIVE" else "PAUSED",
                            color = if (isTimerRunning) SageSuccess else SageWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.size(38.dp))
            }
        }

        // Academic Topic Display
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = academicContext?.courseName ?: "Operating Systems",
                    color = SagePrimaryLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = academicContext?.topic ?: "Process Scheduling",
                    color = SageTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Countdown Timer Display
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_timer_card"),
                level = GlassLevel.L2,
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = SageTextPrimary,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        modifier = Modifier.testTag("focus_timer_text")
                    )

                    // Timer Controls: Pause/Resume
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("focus_play_pause_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimerRunning) SageGlassL3 else SagePrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isTimerRunning) "Pause" else "Resume",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTimerRunning) "Pause" else "Resume",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Current Goal Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_focus_goal_card"),
                level = GlassLevel.L1,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "CURRENT GOAL",
                        color = SageGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = goal.ifBlank { "Understand the key concept and solve one core practice problem." },
                        color = SageTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Session Tasks / Checkpoints
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_tasks_card"),
                level = GlassLevel.L2,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SESSION CHECKPOINTS",
                        color = SagePrimaryLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    FocusTaskRow(
                        title = "Learn the concept",
                        isCompleted = learnCompleted,
                        onClick = onToggleLearn,
                        tag = "focus_task_learn"
                    )

                    FocusTaskRow(
                        title = "Practice core problem",
                        isCompleted = practiceCompleted,
                        onClick = onTogglePractice,
                        tag = "focus_task_practice"
                    )

                    FocusTaskRow(
                        title = "Review & active recall",
                        isCompleted = reviewCompleted,
                        onClick = onToggleReview,
                        tag = "focus_task_review"
                    )
                }
            }
        }

        // Action Toolbar: I'm Stuck, Open Study Chat, Finish Session
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // I'm Stuck Button
                    OutlinedButton(
                        onClick = onStuck,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("focus_im_stuck_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SageGlassL2,
                            contentColor = SageGold
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageGold.copy(alpha = 0.5f))
                    ) {
                        Text(text = "😕", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I'm Stuck",
                            color = SageGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Open Study Chat Button
                    Button(
                        onClick = onOpenStudyChat,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("focus_open_chat_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SagePrimaryStart
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Study Chat",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Finish Session Button
                GlassButton(
                    text = "Finish Session",
                    onClick = onFinishSession,
                    icon = Icons.Default.Flag,
                    variant = GlassButtonVariant.Secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("focus_finish_session_button"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun FocusTaskRow(
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCompleted) SageSuccess.copy(alpha = 0.12f) else SageGlassL1)
            .border(
                1.dp,
                if (isCompleted) SageSuccess.copy(alpha = 0.35f) else SageGlassBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = if (isCompleted) SageTextPrimary else SageTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Medium
        )

        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (isCompleted) "Completed" else "Incomplete",
            tint = if (isCompleted) SageSuccess else SageTextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * 3. Focus Session Summary View
 */
@Composable
private fun FocusSummaryView(
    academicContext: AcademicContext?,
    plannedMinutes: Int,
    actualSeconds: Int,
    goal: String,
    learnCompleted: Boolean,
    practiceCompleted: Boolean,
    reviewCompleted: Boolean,
    onReviewWeakArea: () -> Unit,
    onFinish: () -> Unit
) {
    val focusedMinutes = (actualSeconds / 60).coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(SageSuccess.copy(alpha = 0.2f))
                    .border(2.dp, SageSuccess, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎉", fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "FOCUS SESSION COMPLETE",
                color = SageSuccess,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = academicContext?.topic ?: "Curriculum Topic",
                color = SageTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${academicContext?.courseName ?: "Academic Syllabus"} • Focused $focusedMinutes min",
                color = SageTextSecondary,
                fontSize = 13.sp
            )
        }

        // Summary Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_summary_card"),
                level = GlassLevel.L2,
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SESSION ACCOMPLISHMENTS",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    SummaryCheckRow("Learn the concept", learnCompleted)
                    SummaryCheckRow("Practice core problem", practiceCompleted)
                    SummaryCheckRow("Review & active recall", reviewCompleted)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "GOAL SET",
                        color = SageGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = goal.ifBlank { "Focused study on $academicContext?.topic" },
                        color = SageTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Actions
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReviewWeakArea,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("focus_review_weak_area_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SageGlassL2,
                        contentColor = SagePrimaryLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageGlassBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = SagePrimaryLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Review Weak Area in Chat",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("focus_finish_return_home_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SagePrimary
                    )
                ) {
                    Text(
                        text = "Finish & Return Home",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SummaryCheckRow(label: String, isChecked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = SageTextPrimary, fontSize = 13.sp)
        Text(
            text = if (isChecked) "✓ Completed" else "○ Incomplete",
            color = if (isChecked) SageSuccess else SageTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 4. I'm Stuck Bottom Sheet Content
 */
@Composable
private fun ImStuckBottomSheetContent(
    topic: String,
    onActionSelected: (StuckAction) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HOW CAN SAGE HELP?",
                    color = SageGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Stuck on $topic?",
                    color = SageTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SageTextSecondary)
            }
        }

        StuckOptionItem(
            icon = "💡",
            title = "Explain the concept again",
            subtitle = "Breaks down fundamentals with an intuitive analogy",
            onClick = { onActionSelected(StuckAction.EXPLAIN_CONCEPT) },
            tag = "stuck_explain"
        )

        StuckOptionItem(
            icon = "🔍",
            title = "Give me a hint",
            subtitle = "Guides your thinking without revealing the full solution",
            onClick = { onActionSelected(StuckAction.GIVE_HINT) },
            tag = "stuck_hint"
        )

        StuckOptionItem(
            icon = "📖",
            title = "Show a simpler example",
            subtitle = "Walks through a beginner-level example scenario",
            onClick = { onActionSelected(StuckAction.SIMPLER_EXAMPLE) },
            tag = "stuck_simpler_example"
        )

        StuckOptionItem(
            icon = "🔢",
            title = "Solve step-by-step",
            subtitle = "Step-by-step mathematical & conceptual derivation",
            onClick = { onActionSelected(StuckAction.SOLVE_STEP_BY_STEP) },
            tag = "stuck_step_by_step"
        )

        StuckOptionItem(
            icon = "📸",
            title = "Scan my question",
            subtitle = "Use camera to scan your paper problem with Scan & Solve",
            onClick = { onActionSelected(StuckAction.SCAN_QUESTION) },
            tag = "stuck_scan"
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StuckOptionItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        level = GlassLevel.L1,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = SageTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = SageTextSecondary,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SageTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
