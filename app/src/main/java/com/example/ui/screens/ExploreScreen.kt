package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.curriculum.CurriculumCourse
import com.example.data.curriculum.CurriculumDepartment
import com.example.data.roadmap.DevRoadmapSummary
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
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
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

data class ExploreDomainItem(
    val roadmapId: String,
    val emoji: String,
    val category: String,
    val title: String,
    val description: String
)

@Composable
fun ExploreScreen(
    summaries: List<DevRoadmapSummary>,
    roadmapPercentages: Map<String, Int> = emptyMap(),
    onOpenRoadmap: (String) -> Unit,
    onStartTrack: (String) -> Unit,
    onCustomTrack: () -> Unit,
    curriculumDepartments: List<CurriculumDepartment> = emptyList(),
    onSearchCurriculum: ((String, String?) -> List<CurriculumCourse>)? = null,
    onStartCurriculumCourse: ((CurriculumCourse) -> Unit)? = null,
    onOpenStudyTools: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Academic Curriculum") }
    var selectedDeptId by remember { mutableStateOf("cse_aiml") }
    var selectedSemester by remember { mutableStateOf(0) } // 0 = all

    val categories = listOf(
        "Academic Curriculum",
        "All Tracks",
        "Engineering",
        "AI & Data",
        "Systems & Cloud",
        "Product & QA"
    )

    val allTracks = remember {
        listOf(
            ExploreDomainItem(
                roadmapId = "ml-ai",
                emoji = "🧠",
                category = "AI & Data",
                title = "Machine Learning & AI",
                description = "Neural networks, supervised learning, gradient descent & LLMs"
            ),
            ExploreDomainItem(
                roadmapId = "fullstack",
                emoji = "🌐",
                category = "Engineering",
                title = "Web Development",
                description = "Modern frontend, backend architectures, REST & GraphQL APIs"
            ),
            ExploreDomainItem(
                roadmapId = "data-engineer",
                emoji = "🐍",
                category = "AI & Data",
                title = "Python & Data Science",
                description = "Python pipelines, SQL, Pandas, data warehousing & analytics"
            ),
            ExploreDomainItem(
                roadmapId = "cybersecurity",
                emoji = "🔒",
                category = "Systems & Cloud",
                title = "Cybersecurity",
                description = "Network defense, vulnerability assessments & threat modeling"
            ),
            ExploreDomainItem(
                roadmapId = "frontend",
                emoji = "🎨",
                category = "Engineering",
                title = "Frontend Engineering",
                description = "JavaScript, TypeScript, React, state management & web performance"
            ),
            ExploreDomainItem(
                roadmapId = "backend",
                emoji = "⚙️",
                category = "Engineering",
                title = "Backend Architecture",
                description = "Node.js, databases, caching, concurrency & microservices"
            ),
            ExploreDomainItem(
                roadmapId = "mobile",
                emoji = "📱",
                category = "Engineering",
                title = "Mobile App Development",
                description = "Android, Kotlin, Jetpack Compose, iOS & cross-platform"
            ),
            ExploreDomainItem(
                roadmapId = "devops",
                emoji = "☁️",
                category = "Systems & Cloud",
                title = "DevOps & Cloud Engineering",
                description = "CI/CD pipelines, Docker, Kubernetes, Terraform & AWS/GCP"
            ),
            ExploreDomainItem(
                roadmapId = "qa-engineer",
                emoji = "🧪",
                category = "Product & QA",
                title = "QA & Test Engineering",
                description = "Automated testing, unit, integration, and E2E frameworks"
            ),
            ExploreDomainItem(
                roadmapId = "product-manager",
                emoji = "📋",
                category = "Product & QA",
                title = "Product Management",
                description = "Agile roadmaps, metrics, user research & system design"
            )
        )
    }

    val filteredTracks = remember(searchQuery, selectedCategory) {
        if (selectedCategory == "Academic Curriculum") {
            emptyList()
        } else {
            allTracks.filter { track ->
                val matchesSearch = searchQuery.isBlank() ||
                        track.title.contains(searchQuery, ignoreCase = true) ||
                        track.description.contains(searchQuery, ignoreCase = true)

                val matchesCategory = selectedCategory == "All Tracks" || track.category.equals(selectedCategory, ignoreCase = true)

                matchesSearch && matchesCategory
            }
        }
    }

    // Official Curriculum Courses lookup
    val curriculumCourses = remember(searchQuery, selectedCategory, selectedDeptId, selectedSemester) {
        if (onSearchCurriculum != null && (selectedCategory == "Academic Curriculum" || searchQuery.isNotBlank())) {
            val deptFilter = if (selectedDeptId == "all") null else selectedDeptId
            val results = onSearchCurriculum(searchQuery, deptFilter)
            if (selectedSemester > 0) {
                results.filter { it.semester == selectedSemester }
            } else {
                results
            }
        } else {
            emptyList()
        }
    }

    AmbientGlowBackground(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OFFICIAL SYLLABUS & ROADMAPS",
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Explore Learning Tracks",
                            color = SageTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    GlassButton(
                        text = "Custom",
                        icon = Icons.Default.Add,
                        onClick = onCustomTrack,
                        variant = GlassButtonVariant.Secondary,
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("custom_track_button")
                    )
                }
            }

            if (onOpenStudyTools != null) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explore_study_tools_card"),
                        level = GlassLevel.L2,
                        glowColor = SagePrimary.copy(alpha = 0.3f),
                        borderColor = SagePrimaryStart.copy(alpha = 0.5f),
                        onClick = onOpenStudyTools
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "⚡", fontSize = 22.sp)
                                Column {
                                    Text(
                                        text = "Sage Study Tools",
                                        color = SageTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Notes, Flashcards, Mind Maps, Formula Sheets & Scan & Solve",
                                        color = SageTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_field"),
                    placeholder = {
                        Text(
                            text = "Search syllabus (e.g. CS101, Python, Circuits, Algorithms)...",
                            color = SageTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SageTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SageTextPrimary,
                        unfocusedTextColor = SageTextPrimary,
                        focusedBorderColor = SagePrimaryLight,
                        unfocusedBorderColor = SageGlassBorder,
                        focusedContainerColor = SageGlassL2,
                        unfocusedContainerColor = SageGlassL1
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        val chipInteractionSource = remember { MutableInteractionSource() }
                        val isChipPressed by chipInteractionSource.collectIsPressedAsState()

                        val chipScale by animateFloatAsState(
                            targetValue = if (isChipPressed) 0.94f else 1f,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
                            label = "chip_press"
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = chipScale
                                    scaleY = chipScale
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.linearGradient(listOf(SagePrimaryStart, SagePrimary))
                                    } else {
                                        Brush.linearGradient(listOf(SageGlassL1, SageGlassL2))
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) {
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.5f),
                                                SageGlowEnd.copy(alpha = 0.8f)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(listOf(SageGlassBorder, SageGlassBorder))
                                    },
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    interactionSource = chipInteractionSource,
                                    indication = null
                                ) { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("category_chip_$category")
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else SageTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // If Academic Curriculum is selected, show Department & Semester Filters
            if (selectedCategory == "Academic Curriculum") {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "ACADEMIC DEPARTMENTS (${curriculumDepartments.size})",
                            color = SageTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // "All Depts" chip
                            val isAllSelected = selectedDeptId == "all"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isAllSelected) SagePrimary else SageGlassL2)
                                    .border(1.dp, if (isAllSelected) SagePrimaryLight else SageGlassBorder, RoundedCornerShape(14.dp))
                                    .clickable { selectedDeptId = "all" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("dept_chip_all")
                            ) {
                                Text(
                                    text = "All Depts",
                                    color = if (isAllSelected) Color.White else SageTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            curriculumDepartments.forEach { dept ->
                                val isSelected = selectedDeptId == dept.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) SagePrimary else SageGlassL2)
                                        .border(1.dp, if (isSelected) SagePrimaryLight else SageGlassBorder, RoundedCornerShape(14.dp))
                                        .clickable { selectedDeptId = dept.id }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("dept_chip_${dept.id}")
                                ) {
                                    Text(
                                        text = "${dept.icon} ${dept.shortName}",
                                        color = if (isSelected) Color.White else SageTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // Semester horizontal selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isSemAllSelected = selectedSemester == 0
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSemAllSelected) SageGlassL3 else SageGlassL1)
                                    .border(1.dp, if (isSemAllSelected) SageGold.copy(alpha = 0.6f) else SageGlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedSemester = 0 }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("sem_chip_all")
                            ) {
                                Text(
                                    text = "All Sem",
                                    color = if (isSemAllSelected) SageGold else SageTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            (1..8).forEach { sem ->
                                val isSemSelected = selectedSemester == sem
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSemSelected) SageGlassL3 else SageGlassL1)
                                        .border(1.dp, if (isSemSelected) SageGold.copy(alpha = 0.6f) else SageGlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedSemester = sem }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("sem_chip_$sem")
                                ) {
                                    Text(
                                        text = "Sem $sem",
                                        color = if (isSemSelected) SageGold else SageTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Academic Hierarchy Navigation Card
                item {
                    val activeDept = curriculumDepartments.find { it.id == selectedDeptId }
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("academic_hierarchy_card"),
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(16.dp),
                        borderColor = SagePrimary.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ACADEMIC HIERARCHY",
                                        color = SageGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SageSuccess.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "OFFICIAL SYLLABUS",
                                            color = SageSuccess,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "${curriculumCourses.size} Subjects",
                                    color = SagePrimaryLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = if (activeDept != null) {
                                    "${activeDept.icon} ${activeDept.name} (${activeDept.shortName})"
                                } else {
                                    "🏛️ JIS College of Engineering • All Autonomous Departments"
                                },
                                color = SageTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = buildString {
                                    if (activeDept != null) {
                                        append("Programme: ${activeDept.programme} • Regulation: ${activeDept.regulation}")
                                    } else {
                                        append("10 Academic Departments • Regulations R25 / R26")
                                    }
                                    append(" • ")
                                    append(if (selectedSemester == 0) "All Semesters" else "Semester $selectedSemester")
                                },
                                color = SageTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Official Curriculum Course Cards
                item {
                    Text(
                        text = "OFFICIAL SYLLABUS COURSES (${curriculumCourses.size})",
                        color = SageTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(curriculumCourses) { course ->
                    val coursePercent = roadmapPercentages[course.roadmapId] ?: 0
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("curriculum_card_${course.code}"),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header badge row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SageSuccess.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "OFFICIAL",
                                            color = SageSuccess,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${course.departmentName.uppercase()} • ${course.regulation}",
                                        color = SagePrimaryLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "Sem ${course.semester} • ${course.credits} Credits",
                                    color = SageGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Code and Title
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${course.code}: ${course.title}",
                                    color = SageTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${course.modules.size} Official Modules • ${course.contact.ifBlank { "Prescribed" }} Contact Hours • ${course.courseType}",
                                    color = SageTextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            if (course.objectives.isNotBlank()) {
                                Text(
                                    text = course.objectives.take(160) + if (course.objectives.length > 160) "..." else "",
                                    color = SageTextMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            // Progress
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Progress: $coursePercent%",
                                        color = if (coursePercent > 0) SageGold else SageTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (coursePercent > 0) {
                                        Text(
                                            text = if (coursePercent >= 100) "Completed" else "In Progress",
                                            color = if (coursePercent >= 100) SageSuccess else SagePrimaryLight,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { (coursePercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = SagePrimary,
                                    trackColor = SageGlassL1
                                )
                            }

                            // Actions: [ Start Learning ] and [ Syllabus Roadmap ]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassButton(
                                    text = if (coursePercent > 0) "Continue" else "Start",
                                    icon = Icons.Default.PlayArrow,
                                    onClick = {
                                        if (onStartCurriculumCourse != null) {
                                            onStartCurriculumCourse(course)
                                        } else {
                                            onOpenRoadmap(course.roadmapId)
                                        }
                                    },
                                    variant = GlassButtonVariant.Primary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("start_curriculum_${course.code}")
                                )

                                GlassButton(
                                    text = "Roadmap",
                                    icon = Icons.Default.Map,
                                    onClick = { onOpenRoadmap(course.roadmapId) },
                                    variant = GlassButtonVariant.Outline,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("roadmap_curriculum_${course.code}")
                                )
                            }
                        }
                    }
                }
            }

            // Featured tracks (shown when not in Academic Curriculum or when searching)
            if (filteredTracks.isNotEmpty()) {
                item {
                    Text(
                        text = "FEATURED LEARNING PATHS (${filteredTracks.size})",
                        color = SageTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(filteredTracks) { track ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explore_card_${track.title}"),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(SageGlassL3)
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.3f),
                                                    SageGlassBorderGlow.copy(alpha = 0.4f)
                                                )
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = track.emoji, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.category.uppercase(),
                                        color = SagePrimaryLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = track.title,
                                        color = SageTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = track.description,
                                color = SageTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            val percent = roadmapPercentages[track.roadmapId] ?: if (track.roadmapId == "fullstack") 42 else 0
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Progress: $percent%",
                                        color = if (percent > 0) SageGold else SageTextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (percent > 0) {
                                        Text(
                                            text = if (percent >= 100) "Completed" else "In Progress",
                                            color = if (percent >= 100) SageSuccess else SagePrimaryLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { (percent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = SagePrimary,
                                    trackColor = SageGlassL1
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassButton(
                                    text = if (percent > 0) "Continue" else "Start",
                                    icon = Icons.Default.PlayArrow,
                                    onClick = { onStartTrack(track.title) },
                                    variant = GlassButtonVariant.Primary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("start_button_${track.roadmapId}")
                                )

                                GlassButton(
                                    text = "Roadmap",
                                    icon = Icons.Default.Map,
                                    onClick = { onOpenRoadmap(track.roadmapId) },
                                    variant = GlassButtonVariant.Outline,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("roadmap_button_${track.roadmapId}")
                                )
                            }
                        }
                    }
                }
            }

            // Section: ALL DEVROADMAPS (20 Curated Paths) - Shown if not in Academic Curriculum or All Tracks
            if (selectedCategory != "Academic Curriculum") {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "DEVROADMAPS DIRECTORY (20 PATHS)",
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Curated foundational roadmaps from devroadmaps",
                            color = SageTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                items(summaries) { summary ->
                    val summaryPercent = roadmapPercentages[summary.id] ?: if (summary.id == "fullstack") 42 else 0
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("summary_card_${summary.id}"),
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = summary.icon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = summary.title,
                                        color = SageTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${summary.nodes} Topics • ${summary.difficulty} • Progress: $summaryPercent%",
                                        color = if (summaryPercent > 0) SageGold else SageTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (summaryPercent > 0) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlassButton(
                                    text = if (summaryPercent > 0) "Continue" else "Start",
                                    onClick = { onStartTrack(summary.title) },
                                    variant = GlassButtonVariant.Primary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("directory_continue_${summary.id}")
                                )

                                GlassButton(
                                    text = "Roadmap",
                                    icon = Icons.Default.Map,
                                    onClick = { onOpenRoadmap(summary.id) },
                                    variant = GlassButtonVariant.Secondary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("directory_roadmap_${summary.id}")
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
