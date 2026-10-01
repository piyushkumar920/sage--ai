package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mission.DailyMissionEntity
import com.example.data.mission.MissionTask
import com.example.data.studytools.AcademicContext
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

@Composable
fun DailyMissionCard(
    mission: DailyMissionEntity?,
    onStartMission: (DailyMissionEntity) -> Unit,
    onStartFiveMinuteFocus: (DailyMissionEntity) -> Unit,
    onToggleTask: (taskId: String) -> Unit,
    onTaskAction: (task: MissionTask, context: AcademicContext) -> Unit,
    onReviewMission: (DailyMissionEntity) -> Unit,
    onContinueLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (mission == null) return

    val isCompleted = mission.completed
    val tasks = mission.parseTasks()
    val completedTasks = tasks.count { it.isCompleted }
    val totalTasks = tasks.size
    val progressFraction by animateFloatAsState(
        targetValue = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "daily_mission_progress"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_mission_card"),
        level = GlassLevel.L2,
        shape = RoundedCornerShape(22.dp),
        borderColor = if (isCompleted) SageSuccess.copy(alpha = 0.5f) else SageGold.copy(alpha = 0.45f),
        glowColor = if (isCompleted) SageSuccess.copy(alpha = 0.25f) else SageGold.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCompleted) SageSuccess.copy(alpha = 0.2f)
                                else SageGold.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "✓ MISSION COMPLETE" else "⚡ TODAY'S MISSION",
                            color = if (isCompleted) SageSuccess else SageGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Estimated time badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SageGlassL1)
                        .border(1.dp, SageGlassBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isCompleted) "${mission.estimatedMinutes}m Done" else "${mission.estimatedMinutes} min",
                        color = if (isCompleted) SageSuccess else SageTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Subject / Module & Topic Titles
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val courseTitle = if (mission.courseCode.isNotBlank() && mission.courseName.isNotBlank()) {
                    "${mission.courseCode}: ${mission.courseName}"
                } else {
                    mission.courseName.ifBlank { "Academic Curriculum" }
                }

                Text(
                    text = courseTitle,
                    color = SagePrimaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = mission.topic.ifBlank { mission.module.ifBlank { "Core Learning Topic" } },
                    color = SageTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (!isCompleted) {
                // Goal Statement
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SageGlassL1)
                        .border(1.dp, SageGlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🎯", fontSize = 16.sp)
                        Text(
                            text = mission.goal.ifBlank { "Understand ${mission.topic} and practice active recall." },
                            color = SageTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Progress Indicator (Subtle & Calming)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TASKS",
                            color = SageTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$completedTasks/$totalTasks tasks",
                            color = if (completedTasks == totalTasks) SageSuccess else SageGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SageGold,
                        trackColor = SageGlassL1
                    )
                }

                // Checklist Items
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { task ->
                        val isTaskDone = task.isCompleted
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isTaskDone) SageSuccess.copy(alpha = 0.1f) else SageGlassL1)
                                .border(
                                    1.dp,
                                    if (isTaskDone) SageSuccess.copy(alpha = 0.35f) else SageGlassBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onToggleTask(task.id) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("mission_task_${task.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Interactive Checkbox Circle
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isTaskDone) SageSuccess else Color.Transparent)
                                        .border(
                                            1.5.dp,
                                            if (isTaskDone) SageSuccess else SageTextMuted,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isTaskDone) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.taskType.icon,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = task.title,
                                            color = if (isTaskDone) SageTextMuted else SageTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (isTaskDone) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    }
                                    if (task.description.isNotBlank()) {
                                        Text(
                                            text = task.description,
                                            color = SageTextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }

                            // Quick Open Action Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SagePrimaryStart.copy(alpha = 0.25f))
                                    .clickable { onTaskAction(task, mission.toAcademicContext()) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Open",
                                        color = SagePrimaryLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = SagePrimaryLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons: Start Mission (20m) & 5-Minute Rescue Mode
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onStartMission(mission) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_daily_mission_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Start Mission (${mission.estimatedMinutes} min)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 5-Minute Rescue Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Not ready for 20m?",
                            color = SageTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedButton(
                            onClick = { onStartFiveMinuteFocus(mission) },
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("start_5min_focus_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SageGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = SageGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Start 5-Minute Focus",
                                    color = SageGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // Completed State Layout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SageSuccess.copy(alpha = 0.12f))
                        .border(1.dp, SageSuccess.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "🎉", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "${mission.estimatedMinutes} minutes completed",
                                color = SageSuccess,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All daily tasks mastered. Great consistency today!",
                                color = SageTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onReviewMission(mission) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("review_daily_mission_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageGlassBorder)
                    ) {
                        Text(
                            text = "Review",
                            color = SagePrimaryLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onContinueLearning,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("continue_learning_mission_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        Text(
                            text = "Continue Learning",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
