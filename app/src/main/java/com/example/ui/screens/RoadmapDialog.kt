package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.TopicEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

@Composable
fun RoadmapDialog(
    topic: TopicEntity?,
    onDismiss: () -> Unit
) {
    if (topic == null) return

    val phases = listOf(
        "Step 1: Clarify Goal" to "Identify what practical knowledge or skill you want to achieve.",
        "Step 2: Check Knowledge" to "Diagnostic questions to map out your baseline familiarity.",
        "Step 3: Level Assessment" to "Calibrate depth (Beginner, Foundational, Practitioner, Advanced).",
        "Step 4: Roadmap Construction" to "Custom learning modules tailored to your available time.",
        "Step 5: Concept Teaching" to "Bite-sized explanations paired with comprehension checks.",
        "Step 6: Milestone Quiz" to "Interactive challenge questions (70%+ to pass).",
        "Step 7: Session Summary" to "Recap key understandings, gaps to revisit, and next milestones."
    )

    var dialogEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { dialogEntered = true }

    val dialogScale by animateFloatAsState(
        targetValue = if (dialogEntered) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 500f),
        label = "dialog_scale"
    )
    val dialogY by animateFloatAsState(
        targetValue = if (dialogEntered) 0f else 16f,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 500f),
        label = "dialog_y"
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (dialogEntered) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "dialog_alpha"
    )

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    translationY = dialogY.dp.toPx()
                    alpha = dialogAlpha
                }
                .testTag("roadmap_dialog"),
            level = GlassLevel.L4,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = SageGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Learning Roadmap",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SageTextSecondary
                        )
                    }
                }

                Text(
                    text = "Topic: ${topic.title}",
                    color = SagePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    itemsIndexed(phases) { index, (title, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SageGlassL2)
                                .border(1.dp, SageGlassBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SagePrimary.copy(alpha = 0.2f))
                                    .border(1.dp, SagePrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = SageGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = title,
                                    color = SageTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    color = SageTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Sage guides you through these steps adaptively in chat.",
                        color = SageTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
