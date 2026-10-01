package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.curriculum.CurriculumCourse
import com.example.data.curriculum.CurriculumDepartment
import com.example.data.curriculum.CurriculumModule
import com.example.data.curriculum.CurriculumRepository
import com.example.data.local.TopicEntity
import com.example.data.studytools.AcademicContext
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.components.ModeSelector
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
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

enum class TopicSelectorTab {
    CURRICULUM,
    CHAT_TOPICS
}

@Composable
fun CurriculumTopicSelectorDialog(
    curriculumRepository: CurriculumRepository,
    allTopics: List<TopicEntity>,
    activeTopicId: Long,
    activeAcademicContext: AcademicContext?,
    onSelectCurriculumTopic: (course: CurriculumCourse, module: CurriculumModule, specificTopic: String?) -> Unit,
    onSelectExistingTopic: (TopicEntity) -> Unit,
    onCreateCustomTopic: (title: String, mode: String) -> Unit,
    onDeleteTopic: (TopicEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(TopicSelectorTab.CURRICULUM) }
    var searchQuery by remember { mutableStateOf("") }
    val allDepartments = remember { curriculumRepository.getAllDepartments() }
    var selectedDeptId by remember {
        mutableStateOf(activeAcademicContext?.department?.let { deptName ->
            allDepartments.find { it.name.equals(deptName, ignoreCase = true) || it.shortName.equals(deptName, ignoreCase = true) }?.id
        } ?: allDepartments.firstOrNull()?.id ?: "cse_aiml")
    }
    var selectedSemester by remember {
        mutableIntStateOf(activeAcademicContext?.semester ?: 3)
    }

    var expandedCourseId by remember { mutableStateOf<String?>(activeAcademicContext?.courseCode) }
    var isCreatingCustom by remember { mutableStateOf(false) }
    var customTitle by remember { mutableStateOf("") }
    var customMode by remember { mutableStateOf("NORMAL") }

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    translationY = dialogY.dp.toPx()
                    alpha = dialogAlpha
                }
                .testTag("curriculum_topic_selector_dialog")
        ) {
            GlassCard(
                modifier = Modifier.fillMaxSize(),
                level = GlassLevel.L4,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SageGlassL3)
                                    .border(1.dp, SageGlassBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = SagePrimaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Select Study Topic",
                                    color = SageTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Official syllabus or custom study track",
                                    color = SageTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SageTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SageGlassL2)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == TopicSelectorTab.CURRICULUM) SagePrimary.copy(alpha = 0.85f) else Color.Transparent)
                                .clickable { selectedTab = TopicSelectorTab.CURRICULUM }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (selectedTab == TopicSelectorTab.CURRICULUM) Color.White else SageTextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Curriculum Syllabus",
                                    color = if (selectedTab == TopicSelectorTab.CURRICULUM) Color.White else SageTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == TopicSelectorTab.CHAT_TOPICS) SagePrimary.copy(alpha = 0.85f) else Color.Transparent)
                                .clickable { selectedTab = TopicSelectorTab.CHAT_TOPICS }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (selectedTab == TopicSelectorTab.CHAT_TOPICS) Color.White else SageTextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Custom Topics",
                                    color = if (selectedTab == TopicSelectorTab.CHAT_TOPICS) Color.White else SageTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedTab == TopicSelectorTab.CURRICULUM) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search course or topic (e.g. Operating Systems)", color = SageTextMuted, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SagePrimaryLight, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = SageTextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("curriculum_search_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SagePrimary,
                                unfocusedBorderColor = SageGlassBorder,
                                focusedTextColor = SageTextPrimary,
                                unfocusedTextColor = SageTextPrimary,
                                cursorColor = SagePrimaryLight,
                                focusedContainerColor = SageGlassL2,
                                unfocusedContainerColor = SageGlassL2
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (searchQuery.isBlank()) {
                            // Department Filter Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(allDepartments) { dept ->
                                    val isSelected = dept.id == selectedDeptId
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) SagePrimary.copy(alpha = 0.35f) else SageGlassL2)
                                            .border(1.dp, if (isSelected) SagePrimary else SageGlassBorder, RoundedCornerShape(10.dp))
                                            .clickable { selectedDeptId = dept.id }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = dept.icon, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = dept.shortName,
                                                color = if (isSelected) SageTextPrimary else SageTextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Semester Filter Chips
                            val selectedDept = allDepartments.find { it.id == selectedDeptId }
                            val semesters = selectedDept?.availableSemesters ?: listOf(1, 2, 3, 4, 5, 6, 7, 8)

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(semesters) { sem ->
                                    val isSelected = sem == selectedSemester
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) SagePrimaryStart.copy(alpha = 0.4f) else SageGlassL1)
                                            .border(1.dp, if (isSelected) SagePrimaryStart else SageGlassBorder, RoundedCornerShape(8.dp))
                                            .clickable { selectedSemester = sem }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "Semester $sem",
                                            color = if (isSelected) SagePrimaryLight else SageTextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Courses List
                        val courses = if (searchQuery.isNotBlank()) {
                            curriculumRepository.searchCourses(searchQuery)
                        } else {
                            curriculumRepository.getCoursesForSemester(selectedDeptId, selectedSemester)
                        }

                        if (courses.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No curriculum topics matching '$searchQuery'" else "No courses found for this semester",
                                    color = SageTextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(courses) { course ->
                                    val isExpanded = expandedCourseId == course.code || searchQuery.isNotBlank()
                                    val isCurrentCourse = activeAcademicContext?.courseCode == course.code

                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        level = if (isCurrentCourse) GlassLevel.L3 else GlassLevel.L1,
                                        borderColor = if (isCurrentCourse) SagePrimary.copy(alpha = 0.7f) else SageGlassBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expandedCourseId = if (isExpanded) null else course.code
                                                    },
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(SagePrimary.copy(alpha = 0.2f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = course.code,
                                                                color = SagePrimaryLight,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Sem ${course.semester} • ${course.category}",
                                                            color = SageTextMuted,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = course.title,
                                                        color = SageTextPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        expandedCourseId = if (isExpanded) null else course.code
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = null,
                                                        tint = SageTextSecondary
                                                    )
                                                }
                                            }

                                            // Expanded Modules
                                            AnimatedVisibility(visible = isExpanded) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    course.modules.forEach { module ->
                                                        val isCurrentModule = isCurrentCourse && activeAcademicContext?.module == module.title

                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(if (isCurrentModule) SagePrimary.copy(alpha = 0.25f) else SageGlassL2)
                                                                .border(1.dp, if (isCurrentModule) SagePrimaryLight else SageGlassBorder, RoundedCornerShape(10.dp))
                                                                .clickable {
                                                                    onSelectCurriculumTopic(course, module, null)
                                                                    onDismiss()
                                                                }
                                                                .padding(10.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(
                                                                        text = module.title,
                                                                        color = SageTextPrimary,
                                                                        fontSize = 12.sp,
                                                                        fontWeight = FontWeight.SemiBold
                                                                    )
                                                                    if (module.content.isNotBlank()) {
                                                                        Spacer(modifier = Modifier.height(2.dp))
                                                                        Text(
                                                                            text = module.content,
                                                                            color = SageTextSecondary,
                                                                            fontSize = 10.sp,
                                                                            maxLines = 2
                                                                        )
                                                                    }
                                                                }

                                                                Spacer(modifier = Modifier.width(8.dp))

                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(6.dp))
                                                                        .background(SagePrimaryStart)
                                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                                ) {
                                                                    Text(
                                                                        text = "Study",
                                                                        color = Color.White,
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Custom Topics Tab
                        if (isCreatingCustom) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "CREATE CUSTOM STUDY TRACK",
                                    color = SageGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                OutlinedTextField(
                                    value = customTitle,
                                    onValueChange = { customTitle = it },
                                    label = { Text("Topic or Subject Name", color = SageTextSecondary) },
                                    placeholder = { Text("e.g., Computer Architecture, Quantum Algorithms", color = SageTextMuted) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("custom_topic_title_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SagePrimary,
                                        unfocusedBorderColor = SageGlassBorder,
                                        focusedTextColor = SageTextPrimary,
                                        unfocusedTextColor = SageTextPrimary,
                                        cursorColor = SagePrimaryLight,
                                        focusedContainerColor = SageGlassL2,
                                        unfocusedContainerColor = SageGlassL2
                                    ),
                                    singleLine = true
                                )

                                Text(
                                    text = "SELECT PEDAGOGICAL MODE",
                                    color = SageTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                ModeSelector(
                                    currentMode = customMode,
                                    onModeChanged = { customMode = it }
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    GlassButton(
                                        text = "Cancel",
                                        onClick = { isCreatingCustom = false },
                                        variant = GlassButtonVariant.Secondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    GlassButton(
                                        text = "Start Session",
                                        onClick = {
                                            if (customTitle.isNotBlank()) {
                                                onCreateCustomTopic(customTitle.trim(), customMode)
                                                onDismiss()
                                            }
                                        },
                                        variant = GlassButtonVariant.Primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                GlassButton(
                                    text = "+ New Custom Topic",
                                    onClick = { isCreatingCustom = true },
                                    variant = GlassButtonVariant.Primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("create_custom_topic_button")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (allTopics.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No custom topics yet",
                                            color = SageTextMuted,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(allTopics) { topic ->
                                            val isSelected = topic.id == activeTopicId

                                            GlassCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                level = if (isSelected) GlassLevel.L3 else GlassLevel.L1,
                                                borderColor = if (isSelected) SagePrimary else SageGlassBorder,
                                                shape = RoundedCornerShape(12.dp),
                                                onClick = {
                                                    onSelectExistingTopic(topic)
                                                    onDismiss()
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = topic.title,
                                                            color = SageTextPrimary,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            fontSize = 14.sp
                                                        )
                                                        if (topic.mode.isNotBlank()) {
                                                            Text(
                                                                text = "Mode: ${topic.mode}",
                                                                color = SagePrimaryLight,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                    }

                                                    if (allTopics.size > 1) {
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
