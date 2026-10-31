package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.roadmap.DevRoadmapDetail
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassLevel
import com.example.ui.theme.*

@Composable
fun PersonalRoadmapCreateDialog(
    isGenerating: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onGenerate: (String, (DevRoadmapDetail) -> Unit) -> Unit,
    onRoadmapCreated: (DevRoadmapDetail) -> Unit
) {
    var goalText by remember { mutableStateOf("") }
    var generatedPreview by remember { mutableStateOf<DevRoadmapDetail?>(null) }

    val suggestions = listOf(
        "Prepare for UPSC Civil Services in 1 year",
        "Organic Chemistry for Class 12 Board Exams",
        "Master Python & Machine Learning for Data Science",
        "CA Intermediate Corporate & Other Laws"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                level = GlassLevel.L3,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .testTag("personal_roadmap_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "AI Roadmap Architect",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageTextPrimary
                                )
                                Text(
                                    text = "Powered by Gemini Intelligence",
                                    fontSize = 12.sp,
                                    color = SageTextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SageRaisedSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SageTextSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = SageCardBorder)

                    if (generatedPreview == null) {
                        // Input State
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "Describe your learning goal, competitive exam, or skill in natural language:",
                                    fontSize = 14.sp,
                                    color = SageTextSecondary,
                                    lineHeight = 20.sp
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = goalText,
                                    onValueChange = { goalText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .testTag("roadmap_goal_input"),
                                    placeholder = {
                                        Text(
                                            text = "e.g., I want to master React and Node.js fullstack development in 3 months starting from basics...",
                                            color = SageTextMuted,
                                            fontSize = 14.sp
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SagePrimary,
                                        unfocusedBorderColor = SageCardBorder,
                                        focusedContainerColor = SageSurface.copy(alpha = 0.5f),
                                        unfocusedContainerColor = SageSurface.copy(alpha = 0.3f),
                                        focusedTextColor = SageTextPrimary,
                                        unfocusedTextColor = SageTextPrimary
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            item {
                                Text(
                                    text = "Suggested Goals:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SageTextMuted
                                )
                            }

                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    suggestions.forEach { suggestion ->
                                        GlassChip(
                                            text = suggestion,
                                            selected = false,
                                            onClick = { goalText = suggestion },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            if (errorMessage != null) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SageError.copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, SageError.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = errorMessage,
                                            color = SageError,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Generate Button
                        Button(
                            onClick = {
                                if (goalText.isNotBlank()) {
                                    onGenerate(goalText.trim()) { detail ->
                                        generatedPreview = detail
                                    }
                                }
                            },
                            enabled = goalText.isNotBlank() && !isGenerating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_roadmap_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isGenerating) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Sage is structuring your roadmap...",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                    Text(
                                        text = "Generate AI Roadmap",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // Preview State
                        val preview = generatedPreview!!
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = preview.icon, fontSize = 36.sp)
                                    Column {
                                        Text(
                                            text = preview.title,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SageTextPrimary
                                        )
                                        Text(
                                            text = "Estimated Time: 3 months • Difficulty: Intermediate • ${preview.nodes.size} Milestones",
                                            fontSize = 12.sp,
                                            color = SageGold
                                        )
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = preview.description,
                                    fontSize = 13.sp,
                                    color = SageTextSecondary,
                                    lineHeight = 18.sp
                                )
                            }

                            item {
                                Text(
                                    text = "Curriculum Topics (${preview.nodes.size} milestones):",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageTextPrimary
                                )
                            }

                            items(preview.nodes) { node ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SageRaisedSurface),
                                    border = BorderStroke(1.dp, SageGlassBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = node.icon, fontSize = 24.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = node.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SageTextPrimary
                                            )
                                            Text(
                                                text = node.description,
                                                fontSize = 12.sp,
                                                color = SageTextMuted,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { onRoadmapCreated(preview) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_learning_roadmap_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SageSuccess),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "🚀 Start Learning Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
