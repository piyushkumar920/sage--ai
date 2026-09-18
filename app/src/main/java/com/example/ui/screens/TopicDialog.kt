package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.components.ModeSelector
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

@Composable
fun TopicDialog(
    topics: List<TopicEntity>,
    activeTopicId: Long,
    onSelectTopic: (TopicEntity) -> Unit,
    onCreateTopic: (String, String) -> Unit,
    onDeleteTopic: (TopicEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var isCreating by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newMode by remember { mutableStateOf("NORMAL") }

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
                .testTag("topic_dialog"),
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
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = SagePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCreating) "New Topic" else "Learning Topics",
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

                Spacer(modifier = Modifier.height(12.dp))

                if (isCreating) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("e.g. Quantum Physics, Python Recursion...", color = SageTextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_topic_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SageTextPrimary,
                                unfocusedTextColor = SageTextPrimary,
                                focusedBorderColor = SagePrimary,
                                unfocusedBorderColor = SageCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "Initial Mode:",
                            color = SageTextSecondary,
                            fontSize = 13.sp
                        )

                        ModeSelector(
                            currentMode = newMode,
                            onModeChanged = { newMode = it }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassButton(
                                text = "Cancel",
                                onClick = { isCreating = false },
                                variant = GlassButtonVariant.Ghost
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            GlassButton(
                                text = "Create",
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        onCreateTopic(newTitle, newMode)
                                        isCreating = false
                                        newTitle = ""
                                    }
                                },
                                enabled = newTitle.isNotBlank(),
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier.testTag("create_topic_confirm_button")
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(topics) { topic ->
                            val isSelected = topic.id == activeTopicId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) SagePrimary.copy(alpha = 0.28f) else SageGlassL2)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) SagePrimaryLight else SageGlassBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        onSelectTopic(topic)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("topic_item_${topic.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = topic.title,
                                        color = SageTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (topic.mode == "LEARNING") SagePrimary.copy(alpha = 0.3f)
                                                    else if (topic.mode == "SOCRATIC") SageGold.copy(alpha = 0.3f)
                                                    else SageTextMuted.copy(alpha = 0.2f)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = topic.mode,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (topic.mode == "SOCRATIC") SageGold else SagePrimary
                                            )
                                        }
                                    }
                                }

                                if (topics.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteTopic(topic) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete topic",
                                            tint = SageError.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassButton(
                        text = "Start New Topic",
                        onClick = { isCreating = true },
                        icon = Icons.Default.Add,
                        variant = GlassButtonVariant.Primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_topic_button"),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }
    }
}
