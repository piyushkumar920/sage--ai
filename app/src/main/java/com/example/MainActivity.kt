package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.TopicEntity
import com.example.ui.SageViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConnectionTestScreen
import com.example.ui.screens.DiagnosticScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.RoadmapDialog
import com.example.ui.screens.TopicDialog
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
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
    viewModel: SageViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var inChatScreen by remember { mutableStateOf(false) }
    var hasEnteredApp by remember { mutableStateOf(false) }
    var showDiagnosticsFromTest by remember { mutableStateOf(false) }

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
                apiKey = viewModel.apiKey,
                onRunTest = { viewModel.testAiConnection() },
                onSaveBackendUrl = { viewModel.saveBackendUrl(it) },
                onSaveApiKey = { viewModel.saveApiKey(it) },
                onClearApiKey = { viewModel.clearApiKey() },
                onBack = {
                    showDiagnosticsFromTest = false
                    viewModel.testAiConnection()
                }
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

    if (inChatScreen) {
        ChatScreen(
            activeTopic = activeTopic,
            messages = messages,
            isGenerating = isGenerating,
            isOnline = isOnline,
            currentMode = currentMode,
            streakDays = viewModel.streakDays,
            onSendMessage = { viewModel.sendMessage(it) },
            onRetryMessage = { viewModel.retryMessage(it) },
            onModeChanged = { viewModel.setMode(it) },
            onOpenTopics = { showTopicDialog = true },
            onOpenRoadmap = {
                roadmapTopic = activeTopic
                showRoadmapDialog = true
            },
            onOpenDiagnostics = {
                inChatScreen = false
                currentTab = NavTab.SETTINGS
            },
            onTestConnection = { viewModel.testAiConnection() },
            onNavigateBack = { inChatScreen = false }
        )
    } else {
        Scaffold(
            containerColor = SageBackground,
            contentWindowInsets = WindowInsets.navigationBars,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { inChatScreen = true },
                    containerColor = SagePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("fab_open_chat")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask Sage",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ask Sage",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = SageSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = SageCardBorder,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .testTag("bottom_navigation_bar")
                ) {
                    // 1. Home
                    NavigationBarItem(
                        selected = currentTab == NavTab.HOME,
                        onClick = { currentTab = NavTab.HOME },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Home",
                                fontWeight = if (currentTab == NavTab.HOME) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = SagePrimary,
                            unselectedIconColor = SageTextMuted,
                            unselectedTextColor = SageTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // 2. Explore
                    NavigationBarItem(
                        selected = currentTab == NavTab.EXPLORE,
                        onClick = { currentTab = NavTab.EXPLORE },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = "Explore",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Explore",
                                fontWeight = if (currentTab == NavTab.EXPLORE) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = SagePrimary,
                            unselectedIconColor = SageTextMuted,
                            unselectedTextColor = SageTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_explore")
                    )

                    // 3. Progress
                    NavigationBarItem(
                        selected = currentTab == NavTab.PROGRESS,
                        onClick = { currentTab = NavTab.PROGRESS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Progress",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Progress",
                                fontWeight = if (currentTab == NavTab.PROGRESS) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = SagePrimary,
                            unselectedIconColor = SageTextMuted,
                            unselectedTextColor = SageTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_progress")
                    )

                    // 4. Settings
                    NavigationBarItem(
                        selected = currentTab == NavTab.SETTINGS,
                        onClick = { currentTab = NavTab.SETTINGS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Settings",
                                fontWeight = if (currentTab == NavTab.SETTINGS) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = SagePrimary,
                            unselectedIconColor = SageTextMuted,
                            unselectedTextColor = SageTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavTab.HOME -> {
                        HomeScreen(
                            streakDays = viewModel.streakDays,
                            onOpenTrack = { trackTitle ->
                                viewModel.openTrack(trackTitle)
                                inChatScreen = true
                            },
                            onOpenRoadmap = { trackTitle ->
                                val topic = allTopics.find { it.title.equals(trackTitle, ignoreCase = true) }
                                    ?: activeTopic
                                roadmapTopic = topic
                                showRoadmapDialog = true
                            },
                            onNewTrack = {
                                showTopicDialog = true
                            },
                            onDiscussChallenge = { question, selectedAnswer ->
                                viewModel.discussChallenge(question, selectedAnswer)
                                inChatScreen = true
                            }
                        )
                    }

                    NavTab.EXPLORE -> {
                        ExploreScreen(
                            onStartTrack = { trackTitle ->
                                viewModel.openTrack(trackTitle, "Hi Sage! I want to start learning $trackTitle. Can you assess where we should begin?")
                                inChatScreen = true
                            },
                            onCustomTrack = {
                                showTopicDialog = true
                            }
                        )
                    }

                    NavTab.PROGRESS -> {
                        ProgressScreen(
                            streakDays = viewModel.streakDays,
                            studyMinutes = 45,
                            activeTracksCount = 2,
                            checkpointsCompleted = "1/4",
                            onPracticeConcept = { conceptName ->
                                viewModel.practiceConcept(conceptName)
                                inChatScreen = true
                            }
                        )
                    }

                    NavTab.SETTINGS -> {
                        DiagnosticScreen(
                            isOnline = isOnline,
                            isAiConnected = connectionStatus.isSuccess == true,
                            lastRequestSuccess = viewModel.lastRequestSuccess,
                            lastError = viewModel.lastErrorMessage,
                            lastLatencyMs = viewModel.lastLatencyMs,
                            isTesting = connectionStatus.isChecking,
                            backendUrl = viewModel.backendUrl,
                            apiKey = viewModel.apiKey,
                            onRunTest = { viewModel.testAiConnection() },
                            onSaveBackendUrl = { viewModel.saveBackendUrl(it) },
                            onSaveApiKey = { viewModel.saveApiKey(it) },
                            onClearApiKey = { viewModel.clearApiKey() },
                            onBack = null
                        )
                    }
                }
            }
        }
    }

    if (showRoadmapDialog) {
        RoadmapDialog(
            topic = roadmapTopic ?: activeTopic,
            onDismiss = { showRoadmapDialog = false }
        )
    }

    if (showTopicDialog) {
        TopicDialog(
            topics = allTopics,
            activeTopicId = activeTopic?.id ?: -1L,
            onSelectTopic = {
                viewModel.selectTopic(it)
                showTopicDialog = false
                inChatScreen = true
            },
            onCreateTopic = { title, mode ->
                viewModel.createTopic(title, mode)
                showTopicDialog = false
                inChatScreen = true
            },
            onDeleteTopic = { viewModel.deleteTopic(it) },
            onDismiss = { showTopicDialog = false }
        )
    }
}
