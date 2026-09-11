package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageAccent
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

data class LearningTrackItem(
    val category: String,
    val title: String,
    val phaseTitle: String,
    val currentPhaseIndex: Int,
    val totalPhases: Int,
    val progressPercent: Int
)

@Composable
fun HomeScreen(
    streakDays: Int,
    onOpenTrack: (String) -> Unit,
    onOpenRoadmap: (String) -> Unit,
    onNewTrack: () -> Unit,
    onDiscussChallenge: (question: String, selectedAnswer: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var userStreak by remember { mutableIntStateOf(streakDays) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var challengeCompleted by remember { mutableStateOf(false) }

    val challengeQuestion = "A self-driving vision model achieves 99% accuracy in sunny testing, but crashes in heavy rain. What is the fundamental issue?"
    val challengeOptions = listOf(
        "Underfitting",
        "Distribution Shift (Covariate Shift)",
        "Vanishing Gradient",
        "Learning Rate Too High"
    )
    val correctIndex = 1

    val activeTracks = remember {
        listOf(
            LearningTrackItem(
                category = "TECHNOLOGY & ENGINEERING",
                title = "Machine Learning & AI",
                phaseTitle = "Phase 1: Foundations & The Cost Compass",
                currentPhaseIndex = 1,
                totalPhases = 4,
                progressPercent = 25
            ),
            LearningTrackItem(
                category = "TECHNOLOGY & ENGINEERING",
                title = "Python & Data Science",
                phaseTitle = "Phase 1: Baseline & Diagnostic",
                currentPhaseIndex = 1,
                totalPhases = 4,
                progressPercent = 25
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SageBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good evening,",
                        color = SageTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Learner",
                        color = SageTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Streak Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SageSurface)
                        .border(1.dp, SageGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("streak_pill")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "\uD83D\uDD25",
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${userStreak}d Streak",
                            color = SageGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // DAILY CHALLENGE Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_challenge_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SagePrimaryStart.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Category Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SagePrimaryStart),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Challenge",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "DAILY CHALLENGE",
                            color = SagePrimaryLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Question Text
                    Text(
                        text = challengeQuestion,
                        color = SageTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )

                    // Options List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        challengeOptions.forEachIndexed { index, optionText ->
                            val isSelected = selectedOptionIndex == index
                            val isAnswered = selectedOptionIndex != null
                            val isThisCorrect = index == correctIndex

                            val backgroundColor = when {
                                !isAnswered -> SageRaisedSurface
                                isSelected && isThisCorrect -> SageSuccess.copy(alpha = 0.15f)
                                isSelected && !isThisCorrect -> SageError.copy(alpha = 0.15f)
                                isThisCorrect -> SageSuccess.copy(alpha = 0.10f)
                                else -> SageRaisedSurface.copy(alpha = 0.6f)
                            }

                            val borderColor = when {
                                !isAnswered -> SageCardBorder
                                isSelected && isThisCorrect -> SageSuccess
                                isSelected && !isThisCorrect -> SageError
                                isThisCorrect -> SageSuccess.copy(alpha = 0.5f)
                                else -> SageCardBorder.copy(alpha = 0.5f)
                            }

                            val textColor = when {
                                !isAnswered -> SageTextPrimary
                                isSelected && isThisCorrect -> SageSuccess
                                isSelected && !isThisCorrect -> SageError
                                isThisCorrect -> SageSuccess
                                else -> SageTextSecondary
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(backgroundColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable(enabled = !challengeCompleted) {
                                        selectedOptionIndex = index
                                        challengeCompleted = true
                                        if (index == correctIndex && userStreak == streakDays) {
                                            userStreak += 1
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .testTag("challenge_option_$index")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = optionText,
                                        color = textColor,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected || isThisCorrect && isAnswered) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isAnswered) {
                                        if (isThisCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Correct",
                                                tint = SageSuccess,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Incorrect",
                                                tint = SageError,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Explanation and Sage Discussion action
                    AnimatedVisibility(visible = challengeCompleted, enter = fadeIn()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SageRaisedSurface)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💡 Insight: Distribution shift (covariate shift) occurs when input features change dramatically from training time to inference time, causing brittle models to fail.",
                                color = SageTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        val chosen = challengeOptions.getOrNull(selectedOptionIndex ?: 0) ?: ""
                                        onDiscussChallenge(challengeQuestion, chosen)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Discuss with Sage", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Learning Tracks Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Learning Tracks",
                    color = SageTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onNewTrack,
                    modifier = Modifier.testTag("new_track_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Track",
                        tint = SagePrimaryLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Track",
                        color = SagePrimaryLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active Learning Tracks Cards
        items(activeTracks.size) { index ->
            val track = activeTracks[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTrack(track.title) }
                    .testTag("active_track_card_$index"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top row: category & Map button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = track.category,
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // "Map" action button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SageRaisedSurface)
                                .border(1.dp, SageCardBorder, RoundedCornerShape(12.dp))
                                .clickable { onOpenRoadmap(track.title) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("map_button_$index")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Map",
                                    tint = SagePrimaryLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Map",
                                    color = SageTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Title
                    Text(
                        text = track.title,
                        color = SageTextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Phase description
                    Text(
                        text = track.phaseTitle,
                        color = SageTextSecondary,
                        fontSize = 13.sp
                    )

                    // Linear progress indicator
                    LinearProgressIndicator(
                        progress = { track.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SagePrimary,
                        trackColor = SageRaisedSurface,
                    )

                    // Progress footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Phase ${track.currentPhaseIndex} of ${track.totalPhases}",
                            color = SageTextMuted,
                            fontSize = 12.sp
                        )

                        Text(
                            text = "${track.progressPercent}% Completed",
                            color = SagePrimaryLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
