package com.example.ui.screens.focus

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.curriculum.CurriculumCourse
import com.example.data.curriculum.CurriculumDepartment
import com.example.data.curriculum.CurriculumModule
import com.example.data.curriculum.CurriculumRepository
import com.example.data.profile.AcademicProfile
import com.example.data.studytools.AcademicContext
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

data class TopicSearchResult(
    val course: CurriculumCourse,
    val module: CurriculumModule,
    val specificTopic: String? = null,
    val matchContext: String = ""
)

/**
 * Dedicated, solid, high-readability full-screen modal surface for changing the Focus study topic.
 *
 * Implements progressive disclosure:
 * Department -> Semester -> Subject -> Module -> Topic
 *
 * Temporary Topic Selection Rule:
 * Selecting a topic here updates Focus Setup (Course, Module, Topic, Goal) without mutating
 * the user's permanent AcademicProfile.
 */
@Composable
fun ChangeStudyTopicSheet(
    curriculumRepository: CurriculumRepository,
    currentAcademicProfile: AcademicProfile?,
    activeAcademicContext: AcademicContext?,
    onSelectTopicContext: (AcademicContext) -> Unit,
    onDismiss: () -> Unit
) {
    // Intercept hardware/system back button to close selector before leaving Focus Mode
    BackHandler {
        onDismiss()
    }

    val allDepartments = remember { curriculumRepository.getAllDepartments() }

    // Initial context starts from active context or permanent Academic Profile
    val initialDeptId = remember(activeAcademicContext, currentAcademicProfile) {
        val fromContext = activeAcademicContext?.department?.let { deptName ->
            allDepartments.find { it.name.equals(deptName, ignoreCase = true) || it.shortName.equals(deptName, ignoreCase = true) }?.id
        }
        fromContext ?: currentAcademicProfile?.departmentId ?: allDepartments.firstOrNull()?.id ?: "cse_aiml"
    }

    val initialSemester = remember(activeAcademicContext, currentAcademicProfile) {
        activeAcademicContext?.semester ?: currentAcademicProfile?.semester ?: 3
    }

    var selectedDeptId by remember { mutableStateOf(initialDeptId) }
    var selectedSemester by remember { mutableIntStateOf(initialSemester) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedCourseId by remember { mutableStateOf<String?>(activeAcademicContext?.courseCode) }

    // Courses for selected department and semester
    val coursesInSemester = remember(selectedDeptId, selectedSemester) {
        curriculumRepository.getCoursesForSemester(selectedDeptId, selectedSemester)
    }

    val selectedDept = remember(selectedDeptId) {
        allDepartments.find { it.id == selectedDeptId }
    }

    // Search results across curriculum
    val searchResults = remember(searchQuery) {
        if (searchQuery.trim().length < 2) {
            emptyList()
        } else {
            val q = searchQuery.trim()
            val results = mutableListOf<TopicSearchResult>()
            val allCourses = allDepartments.flatMap { curriculumRepository.getCoursesForDepartment(it.id) }

            for (c in allCourses) {
                val courseMatches = c.title.contains(q, ignoreCase = true) || c.code.contains(q, ignoreCase = true)
                for (m in c.modules) {
                    val moduleMatches = m.title.contains(q, ignoreCase = true) || m.content.contains(q, ignoreCase = true)
                    if (courseMatches || moduleMatches) {
                        results.add(
                            TopicSearchResult(
                                course = c,
                                module = m,
                                specificTopic = if (moduleMatches) m.title else null,
                                matchContext = "${c.departmentName} • Sem ${c.semester} • ${c.code}"
                            )
                        )
                    }
                }
            }
            results.take(20)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Solid, opaque backdrop and container to guarantee no background bleed-through
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6080A12)) // Dark solid backdrop
                .testTag("change_study_topic_sheet")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SageRaisedSurface) // Solid opaque container
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                SageGlassBorderGlow.copy(alpha = 0.35f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // --- 1. Top Navigation & Header ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SageGlassL2)
                                    .border(1.dp, SageGlassBorder, CircleShape)
                                    .testTag("change_topic_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = SageTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Change Study Topic",
                                    color = SageTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Select your study context",
                                    color = SageTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SageTextSecondary
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SageCardBorder.copy(alpha = 0.6f))
                    )

                    // Scrollable Body Content
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- 2. Search Field ---
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search subjects or topics (e.g. Operating Systems, Round Robin)",
                                        color = SageTextMuted,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = SagePrimaryLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = SageTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SagePrimary,
                                    unfocusedBorderColor = SageGlassBorder,
                                    focusedContainerColor = SageGlassL2,
                                    unfocusedContainerColor = SageGlassL1,
                                    focusedTextColor = SageTextPrimary,
                                    unfocusedTextColor = SageTextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("change_topic_search_input")
                            )
                        }

                        // --- If Search Query Active: Show Search Results ---
                        if (searchQuery.trim().isNotBlank()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SEARCH RESULTS (${searchResults.size})",
                                        color = SagePrimaryLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            if (searchResults.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(SageGlassL1)
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No subjects or topics matched '$searchQuery'",
                                            color = SageTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                items(searchResults) { result ->
                                    SearchResultCard(
                                        result = result,
                                        onSelect = {
                                            val context = AcademicContext(
                                                department = result.course.departmentName,
                                                programme = result.course.programme,
                                                regulation = result.course.regulation,
                                                semester = result.course.semester,
                                                courseCode = result.course.code,
                                                courseName = result.course.title,
                                                module = result.module.moduleNumber,
                                                topic = result.specificTopic ?: result.module.title,
                                                officialSyllabusContent = result.module.content
                                            )
                                            onSelectTopicContext(context)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        } else {
                            // --- 3. Academic Profile Reference Context Card ---
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SageGlassL1)
                                        .border(1.dp, SageGlassBorder, RoundedCornerShape(14.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(SagePrimary.copy(alpha = 0.2f))
                                                .border(1.dp, SagePrimary.copy(alpha = 0.4f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = SagePrimaryLight,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "PERMANENT ACADEMIC PROFILE",
                                                color = SageTextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = currentAcademicProfile?.let {
                                                    "${it.departmentName} • Semester ${it.semester}"
                                                } ?: "CSE (AI/ML) • Semester 3",
                                                color = SageTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Text(
                                            text = "Default",
                                            color = SageGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SageGold.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // --- 4. DEPARTMENT SELECTOR ---
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "DEPARTMENT",
                                        color = SageTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )

                                    // Horizontally scrollable department pills
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(allDepartments) { dept ->
                                            val isSelected = dept.id == selectedDeptId
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isSelected) SagePrimary else SageGlassL2
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) SagePrimaryLight else SageGlassBorder,
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable {
                                                        selectedDeptId = dept.id
                                                        expandedCourseId = null
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                                    .testTag("dept_pill_${dept.id}"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(text = dept.icon, fontSize = 13.sp)
                                                    Text(
                                                        text = dept.shortName,
                                                        color = if (isSelected) Color.White else SageTextPrimary,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // --- 5. SEMESTER SELECTOR ---
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "SEMESTER",
                                        color = SageTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val availableSems = selectedDept?.availableSemesters ?: (1..8).toList()
                                        for (sem in availableSems) {
                                            val isSelected = sem == selectedSemester
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (isSelected) SagePrimary else SageGlassL2
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) SagePrimaryLight else SageGlassBorder,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable {
                                                        selectedSemester = sem
                                                        expandedCourseId = null
                                                    }
                                                    .padding(vertical = 10.dp)
                                                    .testTag("sem_button_$sem"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$sem",
                                                    color = if (isSelected) Color.White else SageTextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // --- 6. SUBJECTS LIST (Progressive Disclosure) ---
                            item {
                                Text(
                                    text = "SUBJECTS (${coursesInSemester.size})",
                                    color = SagePrimaryLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            if (coursesInSemester.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(SageGlassL1)
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No subjects registered for Semester $selectedSemester in ${selectedDept?.shortName ?: "this department"}.",
                                            color = SageTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                items(coursesInSemester) { course ->
                                    val isExpanded = expandedCourseId == course.code
                                    CourseItemCard(
                                        course = course,
                                        isExpanded = isExpanded,
                                        onToggleExpand = {
                                            expandedCourseId = if (isExpanded) null else course.code
                                        },
                                        onSelectModule = { module, specificTopic ->
                                            val context = AcademicContext(
                                                department = course.departmentName,
                                                programme = course.programme,
                                                regulation = course.regulation,
                                                semester = course.semester,
                                                courseCode = course.code,
                                                courseName = course.title,
                                                module = module.moduleNumber,
                                                topic = specificTopic ?: module.title,
                                                officialSyllabusContent = module.content
                                            )
                                            onSelectTopicContext(context)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }

                        // --- 7. Bottom Cancel Button ---
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SageTextSecondary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("change_topic_cancel_button")
                            ) {
                                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Subject card with progressive disclosure of modules upon tap.
 */
@Composable
private fun CourseItemCard(
    course: CurriculumCourse,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectModule: (module: CurriculumModule, specificTopic: String?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SageGlassL2)
            .border(
                1.dp,
                if (isExpanded) SagePrimary.copy(alpha = 0.5f) else SageGlassBorder,
                RoundedCornerShape(16.dp)
            )
            .testTag("course_card_${course.code}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Course Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = course.code,
                            color = SagePrimaryLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• ${course.modules.size} modules",
                            color = SageTextMuted,
                            fontSize = 11.sp
                        )
                        if (course.credits.isNotBlank()) {
                            Text(
                                text = "${course.credits} Cr",
                                color = SageGold,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SageGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = course.title,
                        color = SageTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExpanded) SagePrimary.copy(alpha = 0.2f) else SageGlassL1)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Hide" else "View Modules",
                            color = if (isExpanded) SagePrimaryLight else SageTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = if (isExpanded) SagePrimaryLight else SageTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expanded Modules Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SageGlassBorder.copy(alpha = 0.5f))
                    )

                    Text(
                        text = "MODULES IN ${course.code}",
                        color = SageTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    course.modules.forEachIndexed { index, module ->
                        ModuleItemRow(
                            module = module,
                            index = index,
                            onSelect = { specificTopic ->
                                onSelectModule(module, specificTopic)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Module item row with clean synopsis and [ Select ] button.
 */
@Composable
private fun ModuleItemRow(
    module: CurriculumModule,
    index: Int,
    onSelect: (specificTopic: String?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SageGlassL1)
            .border(1.dp, SageGlassBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Module ${index + 1}",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = module.title,
                        color = SageTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onSelect(null) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SagePrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("select_module_${module.id}")
                ) {
                    Text(
                        text = "Select",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (module.content.isNotBlank()) {
                Text(
                    text = module.content.take(160) + if (module.content.length > 160) "..." else "",
                    color = SageTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Search result card showing rich contextual match.
 */
@Composable
private fun SearchResultCard(
    result: TopicSearchResult,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SageGlassL2)
            .border(1.dp, SageGlassBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("search_result_${result.course.code}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.matchContext,
                    color = SagePrimaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = result.course.title,
                    color = SageTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${result.module.moduleNumber}: ${result.specificTopic ?: result.module.title}",
                    color = SageTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onSelect,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SagePrimary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Select",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
