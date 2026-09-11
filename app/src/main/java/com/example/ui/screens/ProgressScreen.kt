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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageWarning

data class FlaggedConceptItem(
    val title: String,
    val topicContext: String
)

@Composable
fun ProgressScreen(
    streakDays: Int,
    studyMinutes: Int = 45,
    activeTracksCount: Int = 2,
    checkpointsCompleted: String = "1/4",
    onPracticeConcept: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val flaggedConcepts = listOf(
        FlaggedConceptItem("Loss Surface Geometry & Local Minima", "Machine Learning & AI"),
        FlaggedConceptItem("Learning Rate Decay Schedules", "Machine Learning & AI"),
        FlaggedConceptItem("Vanishing Gradients in Deep Stacks", "Machine Learning & AI")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SageBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = "MASTERY METRICS",
                    color = SagePrimaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Learning Progress",
                    color = SageTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 2x2 Metric Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: STREAK & STUDY TIME
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "STREAK",
                        iconSymbol = "\uD83D\uDD25", // flame
                        value = "${streakDays} Days",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "STUDY TIME",
                        iconSymbol = "\u23F1\uFE0F", // stopwatch
                        value = "${studyMinutes}m",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: ACTIVE TRACKS & CHECKPOINTS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "ACTIVE TRACKS",
                        iconSymbol = "\u2728", // sparkles
                        value = "$activeTracksCount",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "CHECKPOINTS",
                        iconSymbol = "\u2754", // question
                        value = checkpointsCompleted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section: Concepts to Revisit Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Concepts to Revisit",
                    color = SageTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Amber Pill Badge: "3 flagged"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SageWarning.copy(alpha = 0.15f))
                        .border(1.dp, SageWarning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${flaggedConcepts.size} flagged",
                        color = SageWarning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Concepts to Revisit List
        items(flaggedConcepts.size) { index ->
            val concept = flaggedConcepts[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("flagged_concept_card_$index"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = concept.title,
                            color = SageTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // "▶ Practice" Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SageRaisedSurface)
                            .border(1.dp, SagePrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable { onPracticeConcept(concept.title) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("practice_concept_button_$index")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "▶",
                                color = SagePrimaryLight,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Practice",
                                color = SagePrimaryLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section: Milestone Checkpoint History Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Milestone Checkpoint History",
                color = SageTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Milestone Checkpoint Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("milestone_checkpoint_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Phase 1: Diagnostic & Intuition",
                            color = SageTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Green Pill: "85% PASSED"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SageSuccess.copy(alpha = 0.15f))
                                .border(1.dp, SageSuccess.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "85% PASSED",
                                color = SageSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Strengths: Solid intuition regarding overfitting and real-world failure modes. Ready for Phase 2 optimization mechanics.",
                        color = SageTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    iconSymbol: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SageSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = SageTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = iconSymbol,
                    fontSize = 15.sp
                )
            }

            Text(
                text = value,
                color = SageTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
