package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiResult
import com.example.data.local.DailyQuizRecordEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.SageDatabase
import com.example.data.local.TopicEntity
import com.example.data.local.TopicProgressEntity
import com.example.data.local.WeakConceptEntity
import com.example.data.quiz.QuizQuestion
import com.example.data.quiz.QuizService
import com.example.data.repository.DailyQuizDashboardStats
import com.example.data.repository.DailyQuizHistoryItem
import com.example.data.repository.RoadmapRepository
import com.example.data.repository.SageRepository
import com.example.data.roadmap.DevRoadmapDetail
import com.example.data.roadmap.DevRoadmapNode
import com.example.data.roadmap.DevRoadmapSummary
import com.example.data.roadmap.TopicStatus
import com.example.util.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConnectionStatus(
    val isChecking: Boolean = false,
    val isSuccess: Boolean? = null,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SageViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val geminiClient = GeminiClient(
        backendUrlProvider = { preferencesManager.customBackendUrl.ifEmpty { null } }
    )
    private val database = SageDatabase.getInstance(application)
    private val dao = database.sageDao()
    private val repository = SageRepository(
        dao = dao,
        geminiClient = geminiClient,
        preferencesManager = preferencesManager
    )
    val roadmapRepository = RoadmapRepository(
        context = application,
        dao = dao,
        preferencesManager = preferencesManager
    )
    val quizService = QuizService(geminiClient)
    private val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, networkMonitor.isCurrentlyConnected())

    val allTopics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _activeTopic = MutableStateFlow<TopicEntity?>(null)
    val activeTopic: StateFlow<TopicEntity?> = _activeTopic.asStateFlow()

    val messages: StateFlow<List<MessageEntity>> = _activeTopic.flatMapLatest { topic ->
        if (topic != null) repository.getMessagesForTopic(topic.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _connectionStatus = MutableStateFlow(ConnectionStatus())
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentMode = MutableStateFlow("NORMAL")
    val currentMode: StateFlow<String> = _currentMode.asStateFlow()

    // --- ROADMAP STATE ---
    val allRoadmapSummaries: List<DevRoadmapSummary> = roadmapRepository.getSummaries()

    private val _activeRoadmapId = MutableStateFlow(roadmapRepository.getActiveRoadmapId())
    val activeRoadmapId: StateFlow<String> = _activeRoadmapId.asStateFlow()

    private val _activeRoadmapDetail = MutableStateFlow<DevRoadmapDetail?>(null)
    val activeRoadmapDetail: StateFlow<DevRoadmapDetail?> = _activeRoadmapDetail.asStateFlow()

    val activeRoadmapProgress: StateFlow<List<TopicProgressEntity>> = _activeRoadmapId.flatMapLatest { id ->
        roadmapRepository.getTopicProgressFlow(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val studiedTopics: StateFlow<List<TopicProgressEntity>> = _activeRoadmapId.flatMapLatest { id ->
        roadmapRepository.getStudiedTopicsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _roadmapPercentages = MutableStateFlow<Map<String, Int>>(emptyMap())
    val roadmapPercentages: StateFlow<Map<String, Int>> = _roadmapPercentages.asStateFlow()

    val activeWeakConcepts: StateFlow<List<WeakConceptEntity>> = roadmapRepository.getActiveWeakConcepts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentlyCompletedTopics: StateFlow<List<TopicProgressEntity>> = roadmapRepository.getRecentlyCompletedTopics()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val averageQuizScore: StateFlow<Double?> = roadmapRepository.getAverageQuizScore()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val totalQuizzesCount: StateFlow<Int> = roadmapRepository.getTotalQuizzesCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // --- DAILY QUIZ STATE ---
    private val _todayDailyQuiz = MutableStateFlow<DailyQuizRecordEntity?>(null)
    val todayDailyQuiz: StateFlow<DailyQuizRecordEntity?> = _todayDailyQuiz.asStateFlow()

    private val _dailyQuizHistory = MutableStateFlow<List<DailyQuizHistoryItem>>(emptyList())
    val dailyQuizHistory: StateFlow<List<DailyQuizHistoryItem>> = _dailyQuizHistory.asStateFlow()

    private val _dailyQuizStats = MutableStateFlow<DailyQuizDashboardStats?>(null)
    val dailyQuizStats: StateFlow<DailyQuizDashboardStats?> = _dailyQuizStats.asStateFlow()

    val streakDays: Int
        get() = preferencesManager.streakDays

    val longestStreak: Int
        get() = preferencesManager.longestStreak

    val studyMinutes: Int
        get() = preferencesManager.studyMinutes

    val lastLatencyMs: Long
        get() = preferencesManager.lastLatencyMs

    val lastErrorMessage: String
        get() = preferencesManager.lastErrorMessage

    val lastRequestSuccess: Boolean
        get() = preferencesManager.lastRequestSuccess

    init {
        viewModelScope.launch {
            val topic = repository.getOrCreateDefaultTopic()
            _activeTopic.value = topic
            _currentMode.value = topic.mode

            // Initialize active roadmap
            val currentRoadmapId = preferencesManager.activeRoadmapId
            selectRoadmap(currentRoadmapId)
            refreshRoadmapPercentages()

            // Initialize daily quiz
            loadDailyQuiz()
        }
        testAiConnection()
    }

    fun getRoadmapDetail(roadmapId: String): DevRoadmapDetail? {
        return roadmapRepository.getRoadmapDetail(roadmapId)
    }

    fun refreshRoadmapPercentages() {
        viewModelScope.launch {
            _roadmapPercentages.value = roadmapRepository.getAllRoadmapCompletionPercentages()
        }
    }

    fun selectRoadmap(roadmapId: String) {
        viewModelScope.launch {
            _activeRoadmapId.value = roadmapId
            val detail = roadmapRepository.getRoadmapDetail(roadmapId)
            _activeRoadmapDetail.value = detail
            if (detail != null) {
                roadmapRepository.setActiveRoadmap(roadmapId)
            }
            refreshRoadmapPercentages()
        }
    }

    fun startLearningRoadmapTopic(
        roadmapId: String,
        node: DevRoadmapNode,
        onNavigateToChat: () -> Unit
    ) {
        viewModelScope.launch {
            // 1. Mark topic as IN_PROGRESS & record chat study in Room
            roadmapRepository.recordChatStudyActivity(
                roadmapId = roadmapId,
                nodeId = node.id,
                nodeTitle = node.title,
                category = node.category,
                additionalMinutes = 5,
                targetStatus = "IN_PROGRESS"
            )
            refreshRoadmapPercentages()

            // 2. Build curriculum context
            val roadmapDetail = roadmapRepository.getRoadmapDetail(roadmapId)
            val roadmapTitle = roadmapDetail?.title ?: "Full Stack Developer"
            val progressList = roadmapRepository.getTopicProgressOnce(roadmapId)
            val completedTitles = progressList.filter { it.status == "COMPLETED" }.map { it.nodeTitle }
            val weakConcepts = activeWeakConcepts.value.map { it.concept }

            val curriculumContext = """
Roadmap: $roadmapTitle
Topic: ${node.title}
Category: ${node.category}
Difficulty: ${node.difficulty ?: "Intermediate"}
Description: ${node.description}
Completed Prerequisites: ${if (completedTitles.isEmpty()) "None" else completedTitles.joinToString(", ")}
Known Weak Topics: ${if (weakConcepts.isEmpty()) "None" else weakConcepts.take(3).joinToString(", ")}
            """.trimIndent()

            // 3. Create or select a dedicated Topic in Room for this learning session
            val sessionTitle = "${node.title} — Learning"
            val existingTopic = allTopics.value.find { it.title == sessionTitle }
            val targetTopic = if (existingTopic != null) {
                val updated = existingTopic.copy(
                    mode = "LEARNING",
                    roadmapJson = curriculumContext,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateTopic(updated)
                updated
            } else {
                val newTopic = TopicEntity(
                    title = sessionTitle,
                    mode = "LEARNING",
                    goal = "Master ${node.title} from the $roadmapTitle roadmap",
                    roadmapJson = curriculumContext
                )
                val id = dao.insertTopic(newTopic)
                newTopic.copy(id = id)
            }

            selectTopic(targetTopic)
            setMode("LEARNING")

            // 4. Send initial prompt to trigger structured 7-step teaching session
            sendMessage("Hi Sage! I'm starting the topic '${node.title}' in my $roadmapTitle roadmap. Please begin our structured learning session: explain the core concept, provide a concrete example, and then ask me a comprehension question.")

            // 5. Navigate to Chat
            onNavigateToChat()
        }
    }

    fun askSageAboutTopic(
        roadmapId: String,
        node: DevRoadmapNode,
        onNavigateToChat: () -> Unit
    ) {
        viewModelScope.launch {
            val roadmapDetail = roadmapRepository.getRoadmapDetail(roadmapId)
            val roadmapTitle = roadmapDetail?.title ?: "Curriculum"
            val sessionTitle = "${node.title} Q&A"

            val existingTopic = allTopics.value.find { it.title == sessionTitle }
            val targetTopic = if (existingTopic != null) {
                existingTopic
            } else {
                val newTopic = TopicEntity(
                    title = sessionTitle,
                    mode = "NORMAL",
                    goal = "Deep dive into ${node.title}",
                    roadmapJson = "Roadmap: $roadmapTitle | Topic: ${node.title} | ${node.description}"
                )
                val id = dao.insertTopic(newTopic)
                newTopic.copy(id = id)
            }

            selectTopic(targetTopic)
            setMode("NORMAL")
            sendMessage("Can you give me a high-level briefing on '${node.title}' from the $roadmapTitle curriculum: why it matters and how to approach learning it effectively?")
            onNavigateToChat()
        }
    }

    fun markTopicComplete(
        roadmapId: String,
        nodeId: String,
        nodeTitle: String,
        category: String
    ) {
        viewModelScope.launch {
            val progressList = roadmapRepository.getTopicProgressOnce(roadmapId)
            val current = progressList.find { it.nodeId == nodeId }
            val newStatus = if (current?.status == "COMPLETED") "NOT_STARTED" else "COMPLETED"
            roadmapRepository.setTopicStatus(
                roadmapId = roadmapId,
                nodeId = nodeId,
                nodeTitle = nodeTitle,
                category = category,
                status = newStatus,
                quizScore = if (newStatus == "COMPLETED") 100 else null
            )
        }
    }

    suspend fun loadQuizForTopic(node: DevRoadmapNode): List<QuizQuestion> {
        val roadmapTitle = _activeRoadmapDetail.value?.title ?: "Curriculum"
        return quizService.generateQuizForTopic(node, roadmapTitle)
    }

    fun recordQuizSubmission(
        roadmapId: String,
        topicId: String,
        topicTitle: String,
        score: Int,
        total: Int,
        weakList: List<String>
    ) {
        viewModelScope.launch {
            roadmapRepository.recordQuizResult(
                roadmapId = roadmapId,
                topicId = topicId,
                topicTitle = topicTitle,
                score = score,
                totalQuestions = total,
                weakList = weakList,
                isDailyQuiz = false
            )
        }
    }

    fun loadDailyQuiz() {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val dailyQuiz = roadmapRepository.getOrCreateDailyQuiz(todayStr)
            _todayDailyQuiz.value = dailyQuiz

            _dailyQuizHistory.value = roadmapRepository.getDailyQuizHistory(7)
            _dailyQuizStats.value = roadmapRepository.getDailyQuizStats()
        }
    }

    fun submitDailyQuizAnswer(selectedIndex: Int) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val updated = roadmapRepository.submitDailyQuizAnswer(todayStr, selectedIndex)
            _todayDailyQuiz.value = updated
            _dailyQuizHistory.value = roadmapRepository.getDailyQuizHistory(7)
            _dailyQuizStats.value = roadmapRepository.getDailyQuizStats()
        }
    }

    fun practiceWeakConcept(
        conceptName: String,
        topicContext: String,
        onNavigateToChat: () -> Unit
    ) {
        viewModelScope.launch {
            val title = "Review: $conceptName"
            val newTopic = TopicEntity(
                title = title,
                mode = "LEARNING",
                goal = "Remediate weak concept: $conceptName ($topicContext)",
                roadmapJson = "Weak Concept: $conceptName | Context: $topicContext"
            )
            val id = dao.insertTopic(newTopic)
            val target = newTopic.copy(id = id)
            selectTopic(target)
            setMode("LEARNING")
            sendMessage("I'm reviewing my weak concept: '$conceptName' in $topicContext. Please test me with a targeted problem or thought experiment to help me master it.")
            onNavigateToChat()
        }
    }

    fun testAiConnection() {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus(isChecking = true)
            when (val result = repository.testConnection()) {
                is GeminiResult.Success -> {
                    _connectionStatus.value = ConnectionStatus(
                        isChecking = false,
                        isSuccess = true
                    )
                }
                is GeminiResult.Error -> {
                    _connectionStatus.value = ConnectionStatus(
                        isChecking = false,
                        isSuccess = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun setMode(mode: String) {
        _currentMode.value = mode
        val current = _activeTopic.value ?: return
        viewModelScope.launch {
            val updated = current.copy(mode = mode)
            repository.updateTopic(updated)
            _activeTopic.value = updated
        }
    }

    fun selectTopic(topic: TopicEntity) {
        _activeTopic.value = topic
        _currentMode.value = topic.mode
        preferencesManager.activeTopicId = topic.id
    }

    fun openTrack(title: String, initialPrompt: String? = null) {
        viewModelScope.launch {
            val existing = allTopics.value.find { it.title.equals(title.trim(), ignoreCase = true) }
            val targetTopic = existing ?: repository.createTopic(title.trim(), "NORMAL")
            selectTopic(targetTopic)
            if (!initialPrompt.isNullOrBlank()) {
                sendMessage(initialPrompt)
            }
        }
    }

    fun createTopic(title: String, mode: String) {
        viewModelScope.launch {
            val created = repository.createTopic(title, mode)
            _activeTopic.value = created
            _currentMode.value = created.mode
        }
    }

    fun deleteTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
            if (_activeTopic.value?.id == topic.id) {
                val fallback = repository.getOrCreateDefaultTopic()
                _activeTopic.value = fallback
                _currentMode.value = fallback.mode
            }
        }
    }

    fun sendMessage(text: String) {
        val topic = _activeTopic.value ?: return
        if (text.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                // 1. Build comprehensive roadmap & curriculum overview for Sage context
                val roadmap = _activeRoadmapDetail.value
                val progressList = activeRoadmapProgress.value
                val completed = progressList.filter { it.status == "COMPLETED" }.map { it.nodeTitle }
                val studied = progressList.filter { it.studiedInChat }.map { it.nodeTitle }
                val needsReview = progressList.filter { it.status == "NEEDS_REVIEW" }.map { it.nodeTitle }
                val nextNode = roadmap?.nodes?.find { node ->
                    progressList.find { it.nodeId == node.id }?.status.let { it == null || it == "NOT_STARTED" }
                }

                val roadmapOverview = buildString {
                    appendLine("Active Roadmap: ${roadmap?.title ?: "Full Stack Web Development"}")
                    appendLine("Current Topic / Focus: ${topic.title}")
                    appendLine("Completed Topics (${completed.size}): ${if (completed.isEmpty()) "None yet" else completed.joinToString(", ")}")
                    appendLine("Topics Studied in Chat (${studied.size}): ${if (studied.isEmpty()) "None yet" else studied.joinToString(", ")}")
                    if (needsReview.isNotEmpty()) {
                        appendLine("Topics Needing Review: ${needsReview.joinToString(", ")}")
                    }
                    if (nextNode != null) {
                        appendLine("Next Recommended Roadmap Topic: ${nextNode.title}")
                    }
                    appendLine("Pedagogical Directive: Use the user's roadmap progress to deliver tailored, context-aware teaching.")
                }

                // 2. Track chat study interaction if current topic corresponds to roadmap node
                val activeNode = roadmap?.nodes?.find { node ->
                    topic.title.contains(node.title, ignoreCase = true) ||
                    topic.roadmapJson.contains(node.id, ignoreCase = true)
                }
                if (activeNode != null) {
                    roadmapRepository.recordChatStudyActivity(
                        roadmapId = _activeRoadmapId.value,
                        nodeId = activeNode.id,
                        nodeTitle = activeNode.title,
                        category = activeNode.category,
                        additionalMinutes = 5
                    )
                }

                repository.sendMessage(
                    topicId = topic.id,
                    userText = text,
                    currentMode = _currentMode.value,
                    curriculumContext = roadmapOverview
                )
                refreshRoadmapPercentages()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun retryMessage(messageId: Long) {
        val topic = _activeTopic.value ?: return
        if (_isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                repository.retryMessage(
                    topicId = topic.id,
                    failedMessageId = messageId,
                    currentMode = _currentMode.value
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    val backendUrl: String
        get() = preferencesManager.customBackendUrl.ifEmpty { geminiClient.getEffectiveBackendUrl() }

    val hasCustomBackendUrl: Boolean
        get() = preferencesManager.customBackendUrl.isNotBlank()

    fun saveBackendUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            preferencesManager.customBackendUrl = ""
            testAiConnection()
            return true
        }
        // Strict HTTPS only, reject localhost for production safety
        if (trimmed.startsWith("https://", ignoreCase = true) && !trimmed.contains("localhost", ignoreCase = true)) {
            preferencesManager.customBackendUrl = trimmed
            testAiConnection()
            return true
        }
        return false
    }

    fun resetToDefaults() {
        preferencesManager.customBackendUrl = ""
        testAiConnection()
    }
}
