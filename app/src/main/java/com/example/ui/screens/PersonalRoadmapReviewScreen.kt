package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.roadmap.DevRoadmapDetail
import com.example.data.roadmap.DevRoadmapNode
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.theme.*

@Composable
fun PersonalRoadmapReviewScreen(
    roadmapDetail: DevRoadmapDetail,
    isProcessing: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onUpdateRoadmap: (DevRoadmapDetail) -> Unit,
    onConversationalEdit: (String, (DevRoadmapDetail) -> Unit, (String) -> Unit) -> Unit,
    onRegenerateSection: (String, String, (DevRoadmapDetail) -> Unit, (String) -> Unit) -> Unit,
    onConfirmActivation: (DevRoadmapDetail) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDetail by remember(roadmapDetail) { mutableStateOf(roadmapDetail) }
    var previousDetailStack by remember { mutableStateOf<List<DevRoadmapDetail>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Hierarchy & Overview, 1: AI Conversational Edit, 2: Schedule & Target

    var showEditMetaDialog by remember { mutableStateOf(false) }
    var showAddNodeDialog by remember { mutableStateOf(false) }
    var nodeToDelete by remember { mutableStateOf<DevRoadmapNode?>(null) }

    var aiInstruction by remember { mutableStateOf("") }
    var aiFeedbackMessage by remember { mutableStateOf<String?>(null) }

    var dailyStudyHours by remember(currentDetail) { mutableStateOf(currentDetail.scheduleTarget) }
    var targetDateStr by remember(currentDetail) { mutableStateOf(currentDetail.targetCompletion) }
    var weeklyDays by remember { mutableStateOf("5 days / week") }
    var scheduleConflictWarning by remember { mutableStateOf<String?>(null) }

    fun pushHistory(newState: DevRoadmapDetail) {
        previousDetailStack = previousDetailStack + currentDetail
        currentDetail = newState
        onUpdateRoadmap(newState)
    }

    AmbientGlowBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SageRaisedSurface)
                            .border(1.dp, SageGlassBorder, CircleShape)
                            .testTag("review_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SageTextPrimary
                        )
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ROADMAP STUDIO",
                                color = SageGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SageGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DRAFT",
                                    color = SageGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "${currentDetail.icon} ${currentDetail.title}",
                            color = SageTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (previousDetailStack.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val last = previousDetailStack.last()
                                previousDetailStack = previousDetailStack.dropLast(1)
                                currentDetail = last
                                onUpdateRoadmap(last)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SageRaisedSurface)
                                .border(1.dp, SageGlassBorder, CircleShape)
                                .testTag("undo_roadmap_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo Last Edit",
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showEditMetaDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SageRaisedSurface)
                            .border(1.dp, SageGlassBorder, CircleShape)
                            .testTag("edit_roadmap_meta_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Roadmap Info",
                            tint = SagePrimaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Studio Tabs: Hierarchy, AI Edit, Schedule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    title = "Hierarchy",
                    icon = Icons.Default.AccountTree,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "AI Refine",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Schedule",
                    icon = Icons.Default.Schedule,
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content Area based on selectedTab
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (selectedTab == 0) {
                    // --- TAB 0: HIERARCHY & OVERVIEW ---
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            level = GlassLevel.L2,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = currentDetail.description,
                                    color = SageTextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                HorizontalDivider(color = SageCardBorder)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    OverviewMetric(label = "Milestones", value = currentDetail.nodes.size.toString())
                                    OverviewMetric(label = "Difficulty", value = currentDetail.difficulty)
                                    OverviewMetric(label = "Est. Duration", value = currentDetail.time)
                                    OverviewMetric(label = "Categories", value = currentDetail.categories.size.toString())
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HIERARCHICAL MODULES & TOPICS (${currentDetail.nodes.size})",
                                color = SageTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Button(
                                onClick = { showAddNodeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SageRaisedSurface),
                                border = BorderStroke(1.dp, SageGlassBorder),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_topic_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = SagePrimaryLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Topic", color = SagePrimaryLight, fontSize = 12.sp)
                            }
                        }
                    }

                    items(currentDetail.nodes) { node ->
                        EditableNodeCard(
                            node = node,
                            onDelete = { nodeToDelete = node },
                            onMoveUp = {
                                val idx = currentDetail.nodes.indexOf(node)
                                if (idx > 0) {
                                    val list = currentDetail.nodes.toMutableList()
                                    list.removeAt(idx)
                                    list.add(idx - 1, node)
                                    pushHistory(currentDetail.copy(nodes = list))
                                }
                            },
                            onMoveDown = {
                                val idx = currentDetail.nodes.indexOf(node)
                                if (idx < currentDetail.nodes.size - 1) {
                                    val list = currentDetail.nodes.toMutableList()
                                    list.removeAt(idx)
                                    list.add(idx + 1, node)
                                    pushHistory(currentDetail.copy(nodes = list))
                                }
                            }
                        )
                    }
                } else if (selectedTab == 1) {
                    // --- TAB 1: AI CONVERSATIONAL EDIT ---
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            level = GlassLevel.L2,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SagePrimaryLight)
                                    Text(
                                        text = "Conversational AI Roadmap Refinement",
                                        color = SageTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Tell Sage how you want to customize this roadmap in natural language. For example:\n• \"Add two weeks of advanced practice and mock exams\"\n• \"Make the roadmap beginner-friendly\"\n• \"Focus more on practical projects\"",
                                    color = SageTextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                OutlinedTextField(
                                    value = aiInstruction,
                                    onValueChange = { aiInstruction = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .testTag("ai_instruction_input"),
                                    placeholder = {
                                        Text("e.g. Add 3 more chapters on system design...", color = SageTextMuted)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SagePrimary,
                                        unfocusedBorderColor = SageCardBorder,
                                        focusedContainerColor = SageSurface.copy(alpha = 0.5f),
                                        unfocusedContainerColor = SageSurface.copy(alpha = 0.3f),
                                        focusedTextColor = SageTextPrimary,
                                        unfocusedTextColor = SageTextPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        if (aiInstruction.isNotBlank() && !isProcessing) {
                                            aiFeedbackMessage = null
                                            onConversationalEdit(
                                                aiInstruction.trim(),
                                                { updated ->
                                                    pushHistory(updated)
                                                    aiFeedbackMessage = "Roadmap successfully updated by Sage AI! (Undo available)"
                                                    aiInstruction = ""
                                                },
                                                { err ->
                                                    aiFeedbackMessage = "Failed: $err"
                                                }
                                            )
                                        }
                                    },
                                    enabled = aiInstruction.isNotBlank() && !isProcessing,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_ai_edit_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sage is analyzing changes...", color = Color.White, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Apply AI Refinement", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (aiFeedbackMessage != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SagePrimary.copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, SagePrimaryLight.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = aiFeedbackMessage!!,
                                            color = SageTextPrimary,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 2) {
                    // --- TAB 2: SCHEDULE & TARGET ---
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            level = GlassLevel.L2,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Schedule Customization & Workload Validation",
                                    color = SageTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                ScheduleField(
                                    label = "Daily Study Target",
                                    value = dailyStudyHours,
                                    onValueChange = {
                                        dailyStudyHours = it
                                        pushHistory(currentDetail.copy(scheduleTarget = it))
                                    }
                                )
                                ScheduleField(
                                    label = "Target Completion Window",
                                    value = targetDateStr,
                                    onValueChange = {
                                        targetDateStr = it
                                        pushHistory(currentDetail.copy(targetCompletion = it))
                                    }
                                )
                                ScheduleField(
                                    label = "Weekly Study Days",
                                    value = weeklyDays,
                                    onValueChange = {
                                        weeklyDays = it
                                        scheduleConflictWarning = if (it.contains("1 ") || it.contains("2 ")) {
                                            "Warning: 1-2 study days per week may be insufficient for a 3-month curriculum."
                                        } else {
                                            null
                                        }
                                    }
                                )

                                if (scheduleConflictWarning != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SageWarning.copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, SageWarning.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = scheduleConflictWarning!!,
                                            color = SageWarning,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
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

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Confirmation Action Bar
            Button(
                onClick = { onConfirmActivation(currentDetail) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_and_start_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SageSuccess),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White)
                    Text(
                        text = "Confirm & Start Learning",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- EDIT ROADMAP META DIALOG ---
    if (showEditMetaDialog) {
        EditMetaDialog(
            initialTitle = currentDetail.title,
            initialDescription = currentDetail.description,
            initialDifficulty = currentDetail.difficulty,
            initialTime = currentDetail.time,
            onDismiss = { showEditMetaDialog = false },
            onSave = { newTitle, newDesc, newDiff, newTime ->
                pushHistory(
                    currentDetail.copy(
                        title = newTitle,
                        description = newDesc,
                        difficulty = newDiff,
                        time = newTime
                    )
                )
                showEditMetaDialog = false
            }
        )
    }

    // --- ADD NODE DIALOG ---
    if (showAddNodeDialog) {
        AddNodeDialog(
            onDismiss = { showAddNodeDialog = false },
            onAdd = { title, category, desc ->
                val newNode = DevRoadmapNode(
                    id = "node_${System.currentTimeMillis()}",
                    title = title,
                    icon = "📌",
                    category = category.ifBlank { "fundamentals" },
                    description = desc,
                    resources = emptyList(),
                    children = emptyList()
                )
                pushHistory(currentDetail.copy(nodes = currentDetail.nodes + newNode))
                showAddNodeDialog = false
            }
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    if (nodeToDelete != null) {
        AlertDialog(
            onDismissRequest = { nodeToDelete = null },
            containerColor = SageSurface,
            title = { Text(text = "Remove Topic", color = SageTextPrimary) },
            text = { Text(text = "Are you sure you want to remove \"${nodeToDelete!!.title}\" from this roadmap? This action cannot be undone.", color = SageTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    val target = nodeToDelete!!
                    pushHistory(currentDetail.copy(nodes = currentDetail.nodes.filter { it.id != target.id }))
                    nodeToDelete = null
                }) {
                    Text(text = "Remove", color = SageError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { nodeToDelete = null }) {
                    Text(text = "Cancel", color = SageTextMuted)
                }
            }
        )
    }
}

@Composable
private fun EditMetaDialog(
    initialTitle: String,
    initialDescription: String,
    initialDifficulty: String,
    initialTime: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var difficulty by remember { mutableStateOf(initialDifficulty) }
    var time by remember { mutableStateOf(initialTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SageSurface,
        title = { Text(text = "Edit Roadmap Details", color = SageTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = "Roadmap Title") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SagePrimary, unfocusedBorderColor = SageCardBorder, focusedTextColor = SageTextPrimary, unfocusedTextColor = SageTextPrimary)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Description") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SagePrimary, unfocusedBorderColor = SageCardBorder, focusedTextColor = SageTextPrimary, unfocusedTextColor = SageTextPrimary)
                )
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = { difficulty = it },
                    label = { Text(text = "Difficulty (Beginner/Intermediate/Advanced)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SagePrimary, unfocusedBorderColor = SageCardBorder, focusedTextColor = SageTextPrimary, unfocusedTextColor = SageTextPrimary)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text(text = "Estimated Duration (e.g. 3 months)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SagePrimary, unfocusedBorderColor = SageCardBorder, focusedTextColor = SageTextPrimary, unfocusedTextColor = SageTextPrimary)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    title.ifBlank { initialTitle },
                    description.ifBlank { initialDescription },
                    difficulty.ifBlank { initialDifficulty },
                    time.ifBlank { initialTime }
                )
            }) {
                Text(text = "Save Changes", color = SagePrimaryLight, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = SageTextMuted)
            }
        }
    )
}

@Composable
private fun AddNodeDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("fundamentals") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SageSurface,
        title = { Text(text = "Add Custom Milestone / Topic", color = SageTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = "Topic Title") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SagePrimary, unfocusedBorderColor = SageCardBorder, focusedTextColor = SageTextPrimary, unfocusedTextColor = SageTextPrimary)
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(text = "Category (e.g. fundamentals)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SagePrimary, unfocusedBorderColor = SageCardBorder, focusedTextColor = SageTextPrimary, unfocusedTextColor = SageTextPrimary)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Description & Learning Goals") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SagePrimary, unfocusedBorderColor = SageCardBorder, focusedTextColor = SageTextPrimary, unfocusedTextColor = SageTextPrimary)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onAdd(title.trim(), category.trim(), description.trim())
                }
            }) {
                Text(text = "Add Topic", color = SagePrimaryLight, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = SageTextMuted)
            }
        }
    )
}

@Composable
private fun TabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) SagePrimary else SageRaisedSurface
    val tint = if (isSelected) Color.White else SageTextSecondary
    val border = if (isSelected) null else BorderStroke(1.dp, SageGlassBorder)

    Card(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = border,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, color = tint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun OverviewMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = SageTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = SageTextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun EditableNodeCard(
    node: DevRoadmapNode,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        level = GlassLevel.L1,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = node.icon.ifBlank { "📌" }, fontSize = 22.sp)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SagePrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(text = node.category, color = SagePrimaryLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(text = node.title, color = SageTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (node.description.isNotBlank()) {
                    Text(text = node.description, color = SageTextSecondary, fontSize = 11.sp, maxLines = 2)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = SageTextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = SageTextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SageError, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ScheduleField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, color = SageTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SagePrimary,
                unfocusedBorderColor = SageCardBorder,
                focusedContainerColor = SageSurface.copy(alpha = 0.5f),
                unfocusedContainerColor = SageSurface.copy(alpha = 0.3f),
                focusedTextColor = SageTextPrimary,
                unfocusedTextColor = SageTextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}
