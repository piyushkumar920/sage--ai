package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.TopicEntity
import com.example.data.roadmap.DevRoadmapNode
import com.example.data.roadmap.TopicStatus
import com.example.data.auth.AdminAuthManager
import com.example.ui.SageViewModel
import com.example.ui.admin.AdminConsoleScreen
import com.example.ui.admin.AdminViewModel
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.auth.AuthViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.AuthScreenMode
import com.example.ui.screens.ChatRoadmapScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConnectionTestScreen
import com.example.ui.screens.CurriculumTopicSelectorDialog
import com.example.ui.screens.DailyQuizScreen
import com.example.ui.screens.DiagnosticScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.RoadmapDialog
import com.example.ui.screens.RoadmapScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TopicDetailScreen
import com.example.ui.screens.TopicDialog
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.focus.FocusViewModel
import com.example.ui.studytools.StudyToolsScreen
import com.example.ui.studytools.StudyToolsViewModel
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGlowEnd
import com.example.ui.theme.SageGlowStart
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageTheme

enum class NavTab {
    HOME,
    EXPLORE,
    PROGRESS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SageTheme {
                SageApp()
            }
        }
    }
}

@Composable
fun SageApp(
    viewModel: SageViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var inChatScreen by remember { mutableStateOf(false) }
    var hasEnteredApp by rememberSaveable { mutableStateOf(true) }
    var showDiagnosticsFromTest by remember { mutableStateOf(false) }

    // Sub-screen navigation states
    var inChatRoadmapScreen by remember { mutableStateOf(false) }
    var inStudyToolsScreen by remember { mutableStateOf(false) }
    var viewingRoadmapId by remember { mutableStateOf<String?>(null) }
    var viewingTopicNode by remember { mutableStateOf<DevRoadmapNode?>(null) }
    var takingQuizNode by remember { mutableStateOf<DevRoadmapNode?>(null) }
    var inDailyQuizScreen by remember { mutableStateOf(false) }
    var authScreenMode by remember { mutableStateOf<AuthScreenMode?>(null) }
    var inDiagnosticsScreen by remember { mutableStateOf(false) }
    var inAdminConsoleScreen by remember { mutableStateOf(false) }
    var inFocusScreen by rememberSaveable { mutableStateOf(false) }

    val studyToolsViewModel: StudyToolsViewModel = viewModel()
    val focusViewModel: FocusViewModel = viewModel(
        factory = FocusViewModel.Factory(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        )
    )
    val focusStats by focusViewModel.focusStats.collectAsState()

    var showRoadmapDialog by remember { mutableStateOf(false) }
    var roadmapTopic by remember { mutableStateOf<TopicEntity?>(null) }
    var showTopicDialog by remember { mutableStateOf(false) }

    val activeTopic by viewModel.activeTopic.collectAsState()
    val allTopics by viewModel.allTopics.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()

    // Auto-proceed into app when AI connects successfully
    LaunchedEffect(connectionStatus.isSuccess) {
        if (connectionStatus.isSuccess == true) {
            delay(500)
            hasEnteredApp = true
        }
    }

    // Safety timeout: auto-enter app within 2.5s so user is never stuck on launch screen
    LaunchedEffect(Unit) {
        delay(2500)
        if (!hasEnteredApp && !showDiagnosticsFromTest) {
            hasEnteredApp = true
        }
    }

    // Roadmap state
    val activeRoadmapId by viewModel.activeRoadmapId.collectAsState()
    val activeRoadmapDetail by viewModel.activeRoadmapDetail.collectAsState()
    val activeRoadmapProgress by viewModel.activeRoadmapProgress.collectAsState()
    val roadmapPercentages by viewModel.roadmapPercentages.collectAsState()
    val activeWeakConcepts by viewModel.activeWeakConcepts.collectAsState()
    val recentlyCompletedTopics by viewModel.recentlyCompletedTopics.collectAsState()
    val averageQuizScore by viewModel.averageQuizScore.collectAsState()
    val totalQuizzesCount by viewModel.totalQuizzesCount.collectAsState()

    // Daily quiz state
    val todayDailyQuiz by viewModel.todayDailyQuiz.collectAsState()
    val dailyQuizHistory by viewModel.dailyQuizHistory.collectAsState()
    val dailyQuizStats by viewModel.dailyQuizStats.collectAsState()

    // Daily mission state (Phase C2)
    val todayDailyMission by viewModel.todayMission.collectAsState()
    val recentDailyMissions by viewModel.recentDailyMissions.collectAsState()

    // Academic Profile state (Phase C2.5)
    val academicProfile by viewModel.academicProfile.collectAsState()
    var showAcademicProfileDialog by remember { mutableStateOf(false) }

    // Adaptive Learning & Weekly Review state (Phase C3)
    val weeklyReviewData by viewModel.weeklyReviewData.collectAsState()

    // First gate: Connection Test Screen
    if (!hasEnteredApp) {
        if (showDiagnosticsFromTest) {
            DiagnosticScreen(
                isOnline = isOnline,
                isAiConnected = connectionStatus.isSuccess == true,
                lastRequestSuccess = viewModel.lastRequestSuccess,
                lastError = viewModel.lastErrorMessage,
                lastLatencyMs = viewModel.lastLatencyMs,
                isTesting = connectionStatus.isChecking,
                backendUrl = viewModel.backendUrl,
                onRunTest = { viewModel.testAiConnection() },
                onSaveBackendUrl = { viewModel.saveBackendUrl(it) },
                onBack = {
                    showDiagnosticsFromTest = false
                    viewModel.testAiConnection()
                },
                onResetDefaults = { viewModel.resetToDefaults() }
            )
        } else {
            ConnectionTestScreen(
                isChecking = connectionStatus.isChecking,
                isSuccess = connectionStatus.isSuccess,
                errorMessage = connectionStatus.errorMessage,
                onRetry = { viewModel.testAiConnection() },
                onEnterApp = { hasEnteredApp = true },
                onOpenDiagnostics = { showDiagnosticsFromTest = true }
            )
        }
        return
    }

    // Sub-Screen 0: Dedicated Chat Roadmap Screen
    if (inChatRoadmapScreen) {
        ChatRoadmapScreen(
            activeRoadmapDetail = activeRoadmapDetail,
            progressList = activeRoadmapProgress,
            onBackToChat = { inChatRoadmapScreen = false },
            onContinueLearning = { node ->
                inChatRoadmapScreen = false
                viewModel.startLearningRoadmapTopic(activeRoadmapId, node) {
                    inChatScreen = true
                }
            },
            onViewFullRoadmap = { rId ->
                inChatRoadmapScreen = false
                viewModel.selectRoadmap(rId)
                viewingRoadmapId = rId
            },
            onPracticeWeakTopics = {
                inChatRoadmapScreen = false
                val firstWeak = activeWeakConcepts.firstOrNull()?.concept ?: "Core Concepts"
                viewModel.practiceWeakConcept(firstWeak, activeRoadmapDetail?.title ?: "Curriculum") {
                    inChatScreen = true
                }
            }
        )
        return
    }

    // Sub-Screen 1: Topic Quiz Screen
    if (takingQuizNode != null) {
        val quizNode = takingQuizNode!!
        val currentRoadmapTitle = activeRoadmapDetail?.title ?: "Curriculum"
        QuizScreen(
            node = quizNode,
            roadmapTitle = currentRoadmapTitle,
            onLoadQuiz = { viewModel.loadQuizForTopic(quizNode) },
            onSubmitResult = { score, total, weakList ->
                viewModel.recordQuizSubmission(
                    roadmapId = activeRoadmapId,
                    topicId = quizNode.id,
                    topicTitle = quizNode.title,
                    score = score,
                    total = total,
                    weakList = weakList
                )
            },
            onBack = { takingQuizNode = null },
            onStartLearning = {
                takingQuizNode = null
                viewingTopicNode = null
                viewingRoadmapId = null
                viewModel.startLearningRoadmapTopic(activeRoadmapId, quizNode) {
                    inChatScreen = true
                }
            }
        )
        return
    }

    // Sub-Screen 2: Topic Detail Screen
    if (viewingTopicNode != null) {
        val topicNode = viewingTopicNode!!
        val progressList = activeRoadmapProgress
        val currentStatusStr = progressList.find { it.nodeId == topicNode.id }?.status ?: "NOT_STARTED"
        val status = when (currentStatusStr) {
            "COMPLETED" -> TopicStatus.COMPLETED
            "IN_PROGRESS" -> TopicStatus.IN_PROGRESS
            "NEEDS_REVIEW" -> TopicStatus.NEEDS_REVIEW
            else -> TopicStatus.NOT_STARTED
        }

        TopicDetailScreen(
            node = topicNode,
            roadmapTitle = activeRoadmapDetail?.title ?: "Curriculum",
            status = status,
            onBack = { viewingTopicNode = null },
            onStartLearning = {
                viewingTopicNode = null
                viewingRoadmapId = null
                viewModel.startLearningRoadmapTopic(activeRoadmapId, topicNode) {
                    inChatScreen = true
                }
            },
            onAskSage = {
                viewingTopicNode = null
                viewingRoadmapId = null
                viewModel.askSageAboutTopic(activeRoadmapId, topicNode) {
                    inChatScreen = true
                }
            },
            onTakeQuiz = {
                takingQuizNode = topicNode
            },
            onToggleComplete = {
                viewModel.markTopicComplete(
                    roadmapId = activeRoadmapId,
                    nodeId = topicNode.id,
                    nodeTitle = topicNode.title,
                    category = topicNode.category
                )
            },
            onOpenStudyTools = {
                val roadmapDetail = viewModel.getRoadmapDetail(activeRoadmapId) ?: activeRoadmapDetail
                val curriculumCourse = if (activeRoadmapId.startsWith("curriculum_")) {
                    viewModel.curriculumRepository.getCourseByRoadmapId(activeRoadmapId)
                } else null

                val academicContext = if (curriculumCourse != null) {
                    com.example.data.studytools.AcademicContext(
                        department = curriculumCourse.departmentName,
                        programme = curriculumCourse.programme,
                        regulation = curriculumCourse.regulation,
                        semester = curriculumCourse.semester,
                        courseCode = curriculumCourse.code,
                        courseName = curriculumCourse.title,
                        module = topicNode.title,
                        topic = topicNode.title,
                        officialSyllabusContent = topicNode.description
                    )
                } else {
                    com.example.data.studytools.AcademicContext(
                        module = topicNode.title,
                        topic = topicNode.title,
                        officialSyllabusContent = topicNode.description
                    )
                }

                studyToolsViewModel.setCurriculumContext(
                    topic = topicNode.title,
                    subject = curriculumCourse?.title ?: roadmapDetail?.title ?: "Curriculum Subject",
                    syllabus = topicNode.description,
                    academicContext = academicContext
                )
                inStudyToolsScreen = true
            },
            onOpenFocusMode = {
                val curriculumCourse = if (activeRoadmapId.startsWith("curriculum_")) {
                    viewModel.curriculumRepository.getCourseByRoadmapId(activeRoadmapId)
                } else null

                val academicCtx = if (curriculumCourse != null) {
                    com.example.data.studytools.AcademicContext(
                        department = curriculumCourse.departmentName,
                        programme = curriculumCourse.programme,
                        regulation = curriculumCourse.regulation,
                        semester = curriculumCourse.semester,
                        courseCode = curriculumCourse.code,
                        courseName = curriculumCourse.title,
                        module = topicNode.title,
                        topic = topicNode.title,
                        officialSyllabusContent = topicNode.description
                    )
                } else {
                    com.example.data.studytools.AcademicContext(
                        module = topicNode.title,
                        topic = topicNode.title,
                        officialSyllabusContent = topicNode.description
                    )
                }
                focusViewModel.initializeWithContext(academicCtx)
                viewingTopicNode = null
                inFocusScreen = true
            }
        )
        return
    }

    // Sub-Screen 3: Roadmap Screen (Opens the SPECIFIC roadmap for the selected course)
    if (viewingRoadmapId != null) {
        val roadmapToShow = viewModel.getRoadmapDetail(viewingRoadmapId!!) ?: activeRoadmapDetail
        if (roadmapToShow != null) {
            RoadmapScreen(
                roadmapDetail = roadmapToShow,
                progressList = activeRoadmapProgress,
                onBack = { viewingRoadmapId = null },
                onSelectNode = { node ->
                    viewingTopicNode = node
                }
            )
            return
        }
    }

    // Sub-Screen 4: Daily Quiz Dashboard
    if (inDailyQuizScreen) {
        DailyQuizScreen(
            dailyQuiz = todayDailyQuiz,
            history = dailyQuizHistory,
            stats = dailyQuizStats,
            onSubmitAnswer = { selectedIndex ->
                viewModel.submitDailyQuizAnswer(selectedIndex)
            },
            onBack = { inDailyQuizScreen = false },
            onPracticeWeakConcept = {
                inDailyQuizScreen = false
                val firstWeak = activeWeakConcepts.firstOrNull()?.concept ?: "Core Concepts"
                viewModel.practiceWeakConcept(firstWeak, activeRoadmapDetail?.title ?: "Curriculum") {
                    inChatScreen = true
                }
            }
        )
        return
    }

    // Sub-Screen 4.5: Authentication Screen (Login, Create Account, Forgot Password)
    if (authScreenMode != null) {
        AuthScreen(
            authViewModel = authViewModel,
            initialMode = authScreenMode!!,
            onBack = { authScreenMode = null },
            onAuthSuccess = {
                authScreenMode = null
            }
        )
        return
    }

    // Sub-Screen 4.8: Sage Study Tools Screen
    if (inStudyToolsScreen) {
        StudyToolsScreen(
            viewModel = studyToolsViewModel,
            onBack = { inStudyToolsScreen = false }
        )
        return
    }

    val activeAcademicContext by viewModel.activeAcademicContext.collectAsState()

    // Sub-Screen 4.9: Focus Mode Screen (Distraction-Free Learning Session)
    if (inFocusScreen && !inChatScreen) {
        val activeTopicId = activeTopic?.id
        FocusScreen(
            focusViewModel = focusViewModel,
            curriculumRepository = viewModel.curriculumRepository,
            allTopics = allTopics,
            activeTopicId = activeTopicId,
            currentAcademicProfile = academicProfile,
            onOpenChatWithPrompt = { prompt, context ->
                context?.let { viewModel.setAcademicContext(it) }
                viewModel.sendMessage(prompt)
                inChatScreen = true
            },
            onOpenStudyChat = { context ->
                context?.let { viewModel.setAcademicContext(it) }
                inChatScreen = true
            },
            onOpenScanAndSolve = {
                inStudyToolsScreen = true
            },
            onBack = { inFocusScreen = false }
        )
        return
    }

    // Sub-Screen 5: Chat Screen
    if (inChatScreen) {
        ChatScreen(
            activeTopic = activeTopic,
            messages = messages,
            isGenerating = isGenerating,
            isOnline = isOnline,
            currentMode = currentMode,
            streakDays = viewModel.streakDays,
            academicContext = activeAcademicContext,
            studyToolsViewModel = studyToolsViewModel,
            onSendMessage = { viewModel.sendMessage(it) },
            onRetryMessage = { viewModel.retryMessage(it) },
            onEditMessage = { msgId, newText -> viewModel.editAndResendMessage(msgId, newText) },
            onRetryAi = { msgId -> viewModel.retryAiResponse(msgId) },
            onModeChanged = { viewModel.setMode(it) },
            onOpenTopics = { showTopicDialog = true },
            onOpenRoadmap = {
                inChatRoadmapScreen = true
            },
            onOpenDiagnostics = {
                inChatScreen = false
                currentTab = NavTab.SETTINGS
            },
            onTestConnection = { viewModel.testAiConnection() },
            onNavigateBack = { inChatScreen = false }
        )
    } else {
        // Main Tab Scaffold with unified AmbientGlowBackground
        AmbientGlowBackground {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.navigationBars,
                floatingActionButton = {
                    AskSageFloatingButton(
                        onClick = { inChatScreen = true }
                    )
                },
            bottomBar = {
                com.example.ui.components.GlassDock(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.985f, animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                slideInVertically(initialOffsetY = { 24 }, animationSpec = tween(300, easing = FastOutSlowInEasing)))
                            .togetherWith(fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
                    },
                    label = "tab_crossfade"
                ) { targetTab ->
                    when (targetTab) {
                        NavTab.HOME -> {
                            HomeScreen(
                                streakDays = viewModel.streakDays,
                                activeRoadmapDetail = activeRoadmapDetail,
                                progressList = activeRoadmapProgress,
                                todayDailyQuiz = todayDailyQuiz,
                                todayDailyMission = todayDailyMission,
                                academicProfile = academicProfile,
                                onOpenAcademicProfileDialog = { showAcademicProfileDialog = true },
                                onOpenRoadmap = { roadmapId ->
                                    viewModel.selectRoadmap(roadmapId)
                                    viewingRoadmapId = roadmapId
                                },
                                onContinueLearning = { node ->
                                    viewModel.startLearningRoadmapTopic(activeRoadmapId, node) {
                                        inChatScreen = true
                                    }
                                },
                                onOpenDailyQuiz = {
                                    inDailyQuizScreen = true
                                },
                                onStartNextTopic = { node ->
                                    viewingTopicNode = node
                                },
                                onExploreRoadmaps = {
                                    currentTab = NavTab.EXPLORE
                                },
                                onStartDailyMission = { mission ->
                                    focusViewModel.initializeWithMission(mission)
                                    inFocusScreen = true
                                },
                                onStartFiveMinuteFocus = { mission ->
                                    focusViewModel.startFiveMinuteMissionFocus(mission)
                                    inFocusScreen = true
                                },
                                onToggleDailyMissionTask = { taskId ->
                                    viewModel.toggleMissionTask(taskId)
                                },
                                onDailyMissionTaskAction = { task, context ->
                                    when (task.taskType) {
                                        com.example.data.mission.MissionTaskType.LEARN -> {
                                            viewModel.setAcademicContext(context)
                                            viewModel.sendMessage(
                                                "Hi Sage! I am ready to learn '${task.title}' for ${context.courseCode}: ${context.courseName}. Please break it down clearly."
                                            )
                                            inChatScreen = true
                                        }
                                        com.example.data.mission.MissionTaskType.PRACTICE -> {
                                            studyToolsViewModel.setCurriculumContext(
                                                topic = context.topic,
                                                subject = context.courseName,
                                                syllabus = context.officialSyllabusContent,
                                                academicContext = context
                                            )
                                            studyToolsViewModel.selectTool(com.example.data.studytools.StudyToolType.FLASHCARDS)
                                            inStudyToolsScreen = true
                                        }
                                        com.example.data.mission.MissionTaskType.PYQ -> {
                                            currentTab = NavTab.PROGRESS
                                        }
                                        com.example.data.mission.MissionTaskType.REVIEW -> {
                                            studyToolsViewModel.setCurriculumContext(
                                                topic = context.topic,
                                                subject = context.courseName,
                                                syllabus = context.officialSyllabusContent,
                                                academicContext = context
                                            )
                                            studyToolsViewModel.selectTool(com.example.data.studytools.StudyToolType.REVISION_SHEET)
                                            inStudyToolsScreen = true
                                        }
                                    }
                                },
                                onReviewDailyMission = { mission ->
                                    val ctx = mission.toAcademicContext()
                                    studyToolsViewModel.setCurriculumContext(
                                        topic = ctx.topic,
                                        subject = ctx.courseName,
                                        syllabus = ctx.officialSyllabusContent,
                                        academicContext = ctx
                                    )
                                    studyToolsViewModel.selectTool(com.example.data.studytools.StudyToolType.REVISION_SHEET)
                                    inStudyToolsScreen = true
                                },
                                isAiConnected = (connectionStatus.isSuccess == true),
                                onOpenDiagnostics = {
                                    currentTab = NavTab.SETTINGS
                                },
                                onOpenStudyTools = {
                                    inStudyToolsScreen = true
                                },
                                onOpenFocusMode = {
                                    focusViewModel.initializeWithContext(activeAcademicContext)
                                    inFocusScreen = true
                                }
                            )
                        }

                        NavTab.EXPLORE -> {
                            ExploreScreen(
                                summaries = viewModel.allRoadmapSummaries,
                                roadmapPercentages = roadmapPercentages,
                                curriculumDepartments = viewModel.allCurriculumDepartments,
                                onSearchCurriculum = { query, dept ->
                                    viewModel.searchCurriculumCourses(query, dept)
                                },
                                onStartCurriculumCourse = { course ->
                                    viewModel.selectRoadmap(course.roadmapId)
                                    val detail = viewModel.curriculumRepository.toRoadmapDetail(course)
                                    val firstNode = detail.nodes.firstOrNull()
                                    if (firstNode != null) {
                                        viewModel.startLearningRoadmapTopic(course.roadmapId, firstNode) {
                                            inChatScreen = true
                                        }
                                    } else {
                                        viewModel.openTrack(
                                            "${course.code}: ${course.title}",
                                            "Hi Sage! I am studying '${course.code}: ${course.title}' (${course.departmentName}, Regulation ${course.regulation}). Let's start with Module 1 of the official syllabus."
                                        )
                                        inChatScreen = true
                                    }
                                },
                                onOpenRoadmap = { roadmapId ->
                                    viewModel.selectRoadmap(roadmapId)
                                    viewingRoadmapId = roadmapId
                                },
                                onStartTrack = { trackTitle ->
                                    val roadmap = viewModel.allRoadmapSummaries.find { it.title.equals(trackTitle, ignoreCase = true) }
                                    if (roadmap != null) {
                                        viewModel.selectRoadmap(roadmap.id)
                                    }
                                    viewModel.openTrack(
                                        trackTitle,
                                        "Hi Sage! I want to start learning $trackTitle. Can you assess where we should begin?"
                                    )
                                    inChatScreen = true
                                },
                                onCustomTrack = {
                                    showTopicDialog = true
                                },
                                onOpenStudyTools = {
                                    inStudyToolsScreen = true
                                }
                            )
                        }

                        NavTab.PROGRESS -> {
                            ProgressScreen(
                                activeRoadmapDetail = activeRoadmapDetail,
                                progressList = activeRoadmapProgress,
                                streakDays = viewModel.streakDays,
                                longestStreak = viewModel.longestStreak,
                                quizAverage = averageQuizScore,
                                quizzesCompleted = totalQuizzesCount,
                                recentlyCompleted = recentlyCompletedTopics,
                                weakConcepts = activeWeakConcepts,
                                onSelectTopic = { nodeId ->
                                    val node = activeRoadmapDetail?.nodes?.find { it.id == nodeId }
                                    if (node != null) {
                                        viewingTopicNode = node
                                    }
                                },
                                onPracticeWeakConcept = { concept, topicCtx ->
                                    viewModel.practiceWeakConcept(concept, topicCtx) {
                                        inChatScreen = true
                                    }
                                },
                                onReviewWeakConcept = { concept, context ->
                                    context?.let { viewModel.setAcademicContext(it) }
                                    viewModel.sendMessage(
                                        "Hi Sage! I need to review and master '$concept'. Could you give a concise explanation, break down the core intuition, and give me one targeted question to test my understanding?"
                                    )
                                    inChatScreen = true
                                },
                                onStartReviewSession = { concept, context ->
                                    focusViewModel.initializeForReviewSession(concept, context)
                                    inFocusScreen = true
                                },
                                onOpenRoadmap = { roadmapId ->
                                    viewModel.selectRoadmap(roadmapId)
                                    viewingRoadmapId = roadmapId
                                },
                                academicContext = activeAcademicContext,
                                careerSummaries = viewModel.allRoadmapSummaries,
                                roadmapPercentages = roadmapPercentages,
                                focusStats = focusStats,
                                dailyMissions = recentDailyMissions,
                                weeklyReviewData = weeklyReviewData
                            )
                        }

                        NavTab.SETTINGS -> {
                            if (inAdminConsoleScreen && AdminAuthManager.isAdmin()) {
                                val context = androidx.compose.ui.platform.LocalContext.current
                                val adminViewModel: AdminViewModel = viewModel(
                                    factory = AdminViewModel.Factory(
                                        context.applicationContext as android.app.Application
                                    )
                                )
                                AdminConsoleScreen(
                                    adminViewModel = adminViewModel,
                                    backendUrl = viewModel.backendUrl,
                                    onSaveBackendUrl = { viewModel.saveBackendUrl(it) },
                                    onResetBackendDefaults = { viewModel.resetToDefaults() },
                                    onBack = { inAdminConsoleScreen = false }
                                )
                            } else if (inDiagnosticsScreen) {
                                DiagnosticScreen(
                                    isOnline = isOnline,
                                    isAiConnected = connectionStatus.isSuccess == true,
                                    lastRequestSuccess = viewModel.lastRequestSuccess,
                                    lastError = viewModel.lastErrorMessage,
                                    lastLatencyMs = viewModel.lastLatencyMs,
                                    isTesting = connectionStatus.isChecking,
                                    backendUrl = viewModel.backendUrl,
                                    onRunTest = { viewModel.testAiConnection() },
                                    onSaveBackendUrl = { viewModel.saveBackendUrl(it) },
                                    onBack = { inDiagnosticsScreen = false },
                                    onResetDefaults = { viewModel.resetToDefaults() }
                                )
                            } else {
                                SettingsScreen(
                                    authViewModel = authViewModel,
                                    academicProfile = academicProfile,
                                    onOpenAcademicProfileDialog = { showAcademicProfileDialog = true },
                                    onOpenAuth = { mode ->
                                        authScreenMode = mode
                                    },
                                    onOpenDiagnostics = {
                                        inDiagnosticsScreen = true
                                    },
                                    onOpenDeveloperConsole = {
                                        inAdminConsoleScreen = true
                                    },
                                    backendStatus = if (connectionStatus.isSuccess == true) "Online" else "Checking",
                                    syncStatus = "Active",
                                    lastErrorCode = viewModel.lastErrorMessage
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }

    if (showAcademicProfileDialog) {
        com.example.ui.components.AcademicProfileDialog(
            currentProfile = academicProfile,
            departments = viewModel.allCurriculumDepartments,
            onSaveProfile = { dept, sem ->
                viewModel.saveAcademicProfile(dept, sem)
                showAcademicProfileDialog = false
            },
            onDismiss = { showAcademicProfileDialog = false }
        )
    }

    if (showRoadmapDialog) {
        RoadmapDialog(
            topic = roadmapTopic ?: activeTopic,
            onDismiss = { showRoadmapDialog = false }
        )
    }

    if (showTopicDialog) {
        CurriculumTopicSelectorDialog(
            curriculumRepository = viewModel.curriculumRepository,
            allTopics = allTopics,
            activeTopicId = activeTopic?.id ?: -1L,
            activeAcademicContext = activeAcademicContext,
            onSelectCurriculumTopic = { course, module, specificTopic ->
                viewModel.selectCurriculumTopic(course, module, specificTopic) {
                    showTopicDialog = false
                    inChatScreen = true
                }
            },
            onSelectExistingTopic = {
                viewModel.selectTopic(it)
                showTopicDialog = false
                inChatScreen = true
            },
            onCreateCustomTopic = { title, mode ->
                viewModel.createTopic(title, mode)
                showTopicDialog = false
                inChatScreen = true
            },
            onDeleteTopic = { viewModel.deleteTopic(it) },
            onDismiss = { showTopicDialog = false }
        )
    }
}

/**
 * Floating glass action button for "Ask Sage" with breathing ambient glow,
 * gentle floating animation, and responsive micro-interaction press scaling.
 */
@Composable
fun AskSageFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "fab_breath")
    val ambientGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_glow"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
        label = "fab_scale"
    )

    val pressGlow by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
        label = "fab_press_glow"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                val glowRadius = size.maxDimension * (0.85f + (0.15f * pressGlow))
                val effectiveAlpha = ((ambientGlowAlpha * 0.8f) + (pressGlow * 0.45f)).coerceAtMost(0.95f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SageGlowStart.copy(alpha = 0.65f * effectiveAlpha),
                            SagePrimary.copy(alpha = 0.35f * effectiveAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = center
                )
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        SagePrimaryLight.copy(alpha = 0.88f),
                        SagePrimary.copy(alpha = 0.74f),
                        SagePrimaryStart.copy(alpha = 0.80f)
                    )
                )
            )
            .border(
                width = 1.3.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.80f),
                        SageGlowEnd.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.60f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(x = size.width * 0.15f, y = 1.5f),
                    end = Offset(x = size.width * 0.85f, y = 1.5f),
                    strokeWidth = 2f
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 13.dp)
            .testTag("fab_open_chat")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Ask Sage",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ask Sage",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}
