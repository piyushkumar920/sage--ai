package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SageViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConnectionTestScreen
import com.example.ui.screens.DiagnosticScreen
import com.example.ui.screens.RoadmapDialog
import com.example.ui.screens.TopicDialog
import com.example.ui.theme.SageTheme

enum class Screen {
    CONNECTION_TEST,
    CHAT,
    DIAGNOSTICS
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
    var currentScreen by remember { mutableStateOf(Screen.CONNECTION_TEST) }
    var showRoadmapDialog by remember { mutableStateOf(false) }
    var showTopicDialog by remember { mutableStateOf(false) }

    val activeTopic by viewModel.activeTopic.collectAsState()
    val allTopics by viewModel.allTopics.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()

    when (currentScreen) {
        Screen.CONNECTION_TEST -> {
            ConnectionTestScreen(
                isChecking = connectionStatus.isChecking,
                isSuccess = connectionStatus.isSuccess,
                errorMessage = connectionStatus.errorMessage,
                onRetry = { viewModel.testAiConnection() },
                onEnterApp = { currentScreen = Screen.CHAT },
                onOpenDiagnostics = { currentScreen = Screen.DIAGNOSTICS }
            )
        }

        Screen.CHAT -> {
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
                onOpenRoadmap = { showRoadmapDialog = true },
                onOpenDiagnostics = { currentScreen = Screen.DIAGNOSTICS },
                onTestConnection = { viewModel.testAiConnection() }
            )

            if (showRoadmapDialog) {
                RoadmapDialog(
                    topic = activeTopic,
                    onDismiss = { showRoadmapDialog = false }
                )
            }

            if (showTopicDialog) {
                TopicDialog(
                    topics = allTopics,
                    activeTopicId = activeTopic?.id ?: -1L,
                    onSelectTopic = { viewModel.selectTopic(it) },
                    onCreateTopic = { title, mode -> viewModel.createTopic(title, mode) },
                    onDeleteTopic = { viewModel.deleteTopic(it) },
                    onDismiss = { showTopicDialog = false }
                )
            }
        }

        Screen.DIAGNOSTICS -> {
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
                    currentScreen = if (connectionStatus.isSuccess == true) Screen.CHAT else Screen.CONNECTION_TEST
                }
            )
        }
    }
}
