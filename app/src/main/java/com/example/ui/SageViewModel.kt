package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiResult
import com.example.data.curriculum.CurriculumCourse
import com.example.data.curriculum.CurriculumDepartment
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    // --- AUTH REPOSITORY ---
    val authRepository = com.example.data.auth.FirebaseAuthRepository()

    val roadmapRepository = RoadmapRepository(
        context = application,
        dao = dao,
        preferencesManager = preferencesManager,
        authRepository = authRepository,
        geminiClient = geminiClient
    )
    val curriculumRepository: com.example.data.curriculum.CurriculumRepository
        get() = roadmapRepository.curriculumRepository
    val quizService = QuizService(geminiClient)
    private val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, networkMonitor.isCurrentlyConnected())

    val allTopics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _activeTopic = MutableStateFlow<TopicEntity?>(null)
    val activeTopic: StateFlow<TopicEntity?> = _activeTopic.asStateFlow()

    private val _activeAcademicContext = MutableStateFlow<com.example.data.studytools.AcademicContext?>(null)
    val activeAcademicContext: StateFlow<com.example.data.studytools.AcademicContext?> = _activeAcademicContext.asStateFlow()

    fun setAcademicContext(context: com.example.data.studytools.AcademicContext?) {
        _activeAcademicContext.value = context
    }

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
    val allRoadmapSummaries: List<DevRoadmapSummary>
        get() = roadmapRepository.getSummaries()

    private val _isGeneratingRoadmap = MutableStateFlow(false)
    val isGeneratingRoadmap: StateFlow<Boolean> = _isGeneratingRoadmap.asStateFlow()

    private val _roadmapGenerationError = MutableStateFlow<String?>(null)
    val roadmapGenerationError: StateFlow<String?> = _roadmapGenerationError.asStateFlow()

    fun generatePersonalRoadmap(goalPrompt: String, onSuccess: (DevRoadmapDetail) -> Unit) {
        viewModelScope.launch {
            _isGeneratingRoadmap.value = true
            _roadmapGenerationError.value = null
            when (val res = roadmapRepository.generatePersonalRoadmap(goalPrompt)) {
                is GeminiResult.Success -> {
                    _isGeneratingRoadmap.value = false
                    onSuccess(res.data)
                }
                is GeminiResult.Error -> {
                    _isGeneratingRoadmap.value = false
                    _roadmapGenerationError.value = res.message
                }
            }
        }
    }

    fun updateDraftRoadmap(detail: DevRoadmapDetail) {
        viewModelScope.launch {
            roadmapRepository.updatePersonalRoadmap(detail)
        }
    }

    fun conversationalEditDraftRoadmap(
        currentDetail: DevRoadmapDetail,
        instruction: String,
        onSuccess: (DevRoadmapDetail) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingRoadmap.value = true
            when (val res = roadmapRepository.conversationalEditRoadmap(currentDetail, instruction)) {
                is GeminiResult.Success -> {
                    _isGeneratingRoadmap.value = false
                    onSuccess(res.data)
                }
                is GeminiResult.Error -> {
                    _isGeneratingRoadmap.value = false
                    onError(res.message)
                }
            }
        }
    }

    fun confirmAndActivateRoadmap(detail: DevRoadmapDetail, onActivated: () -> Unit) {
        viewModelScope.launch {
            roadmapRepository.updatePersonalRoadmap(detail)
            dao.updateDocumentGroundedRoadmapStatus(detail.id, "ACTIVE")
            selectRoadmap(detail.id)
            onActivated()
        }
    }

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

    // --- ADAPTIVE LEARNING & WEEKLY REVIEW (PHASE C3) ---
    val adaptiveLearningRepository = com.example.data.adaptive.AdaptiveLearningRepository(
        context = application,
        dao = dao,
        preferencesManager = preferencesManager,
        curriculumRepository = curriculumRepository
    )

    val weeklyReviewData: StateFlow<com.example.data.adaptive.WeeklyReviewData> = adaptiveLearningRepository
        .getWeeklyReviewFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, com.example.data.adaptive.WeeklyReviewData())

    fun applyAdaptiveRecommendation(recommendationId: String, targetConcept: String, onApplied: (String) -> Unit) {
        viewModelScope.launch {
            adaptiveLearningRepository.recordWeakConcept(targetConcept, _activeRoadmapId.value, "", "Applied AI recommendation")
            onApplied("Applied recommendation for '$targetConcept' successfully!")
        }
    }

    // --- DOCUMENT INTELLIGENCE & SYLLABUS EXTRACTION (PHASE 6) ---
    val documentExtractionRepository = com.example.data.documents.DocumentExtractionRepository(
        context = getApplication(),
        geminiClient = geminiClient
    )

    suspend fun extractSyllabusDocument(name: String, mimeType: String, base64: String, text: String): com.example.data.documents.DocumentExtractionResponse? {
        val result = documentExtractionRepository.extractSyllabus(name, mimeType, base64, text)
        return when (result) {
            is GeminiResult.Success -> {
                val data = result.data
                val docId = data.documentId ?: "doc_${System.currentTimeMillis()}"
                val extId = data.extractionId ?: "ext_${System.currentTimeMillis()}"
                // Non-blocking offline persistence
                viewModelScope.launch {
                    dao.insertUploadedDocument(
                        com.example.data.documents.UploadedDocumentEntity(
                            documentId = docId,
                            fileName = name,
                            mimeType = mimeType,
                            fileSizeBytes = base64.length.toLong(),
                            fileSignature = mimeType,
                            validationStatus = "VALIDATED"
                        )
                    )
                    dao.insertDocumentExtraction(
                        com.example.data.documents.DocumentExtractionEntity(
                            extractionId = extId,
                            documentId = docId,
                            confidence = data.structuredData?.extractionSummary?.confidence ?: "HIGH",
                            structuredCurriculumJson = name,
                            verificationStatus = "PENDING_REVIEW"
                        )
                    )
                }
                data
            }
            is GeminiResult.Error -> com.example.data.documents.DocumentExtractionResponse(
                success = false,
                documentId = null,
                extractionId = null,
                structuredData = null,
                processingStatus = "FAILED",
                error = result.message
            )
        }
    }

    fun createGroundedRoadmapDraftFromDocument(
        structuredData: com.example.data.documents.StructuredCurriculumData,
        documentId: String = "doc_${System.currentTimeMillis()}"
    ): DevRoadmapDetail {
        val meta = structuredData.documentMetadata
        val title = meta?.documentTitle?.takeIf { it.isNotBlank() } ?: "Document Grounded Syllabus"
        val desc = "Grounded on approved syllabus${meta?.institution?.let { " from $it" } ?: ""}. Verified topics with source citations."
        val categories = mutableListOf<String>()
        val nodes = mutableListOf<DevRoadmapNode>()

        val semesters = structuredData.semesters ?: emptyList()
        var nodeCount = 0
        for (sem in semesters) {
            val subjects = sem.subjects ?: emptyList()
            for (sub in subjects) {
                val catName = "${sub.subjectName} (Sem ${sem.semesterNumber})"
                if (!categories.contains(catName)) categories.add(catName)
                val modules = sub.modules ?: emptyList()
                for (mod in modules) {
                    val topics = mod.topics ?: emptyList()
                    for (top in topics) {
                        nodeCount++
                        val nodeId = "grounded_node_${nodeCount}"
                        val pageRef = top.sourceReferences?.firstOrNull()?.pageNumber
                        val typeRef = top.sourceReferences?.firstOrNull()?.sourceType ?: "EXPLICIT"
                        val pageDesc = if (pageRef != null) " [Source: Page $pageRef ($typeRef)]" else " [Source: $typeRef]"
                        val objectives = top.learningObjectives?.joinToString("; ") ?: ""
                        val description = "${top.subtopics?.joinToString(", ") ?: top.topicName}$pageDesc. $objectives"

                        nodes.add(
                            DevRoadmapNode(
                                id = nodeId,
                                title = top.topicName,
                                icon = "📄",
                                category = catName,
                                description = description,
                                difficulty = "Intermediate"
                            )
                        )
                    }
                }
            }
        }

        if (nodes.isEmpty()) {
            nodes.add(
                DevRoadmapNode(
                    id = "grounded_node_1",
                    title = "Fundamental Core Concepts",
                    icon = "📄",
                    category = "Overview",
                    description = "Core syllabus foundations extracted from document.",
                    difficulty = "Beginner"
                )
            )
            categories.add("Overview")
        }

        val roadmapId = "personal_doc_${System.currentTimeMillis()}"
        val detail = DevRoadmapDetail(
            id = roadmapId,
            title = title,
            description = desc,
            icon = "📄",
            categories = categories,
            nodes = nodes,
            difficulty = "Intermediate",
            time = "4 months",
            scheduleTarget = "2 hours / day",
            targetCompletion = "4 months"
        )

        viewModelScope.launch {
            dao.insertDocumentGroundedRoadmap(
                com.example.data.documents.DocumentGroundedRoadmapEntity(
                    roadmapId = roadmapId,
                    documentId = documentId,
                    extractionId = "ext_${System.currentTimeMillis()}",
                    status = "DRAFT",
                    groundedConfidence = structuredData.extractionSummary?.confidence ?: "HIGH"
                )
            )
            // Register draft in roadmap repository so it is reviewable and editable
            roadmapRepository.updatePersonalRoadmap(detail)
        }

        return detail
    }

    // --- UNIVERSAL STUDY ATTACHMENTS (STAGE 1 & 2) ---

    val studyAttachmentRepository = com.example.data.attachments.StudyAttachmentRepository(
        context = application,
        dao = dao,
        geminiClient = geminiClient,
        authRepository = authRepository
    )

    val activeConversationAttachments: StateFlow<List<com.example.data.attachments.StudyAttachmentEntity>> = _activeTopic.flatMapLatest { topic ->
        if (topic != null) {
            val uid = authRepository.currentUser?.uid ?: "local_user"
            val context = com.example.data.attachments.AttachmentContext(
                contextType = com.example.data.attachments.AttachmentContextType.CHAT_CONVERSATION,
                contextId = topic.id.toString(),
                ownerUid = uid
            )
            studyAttachmentRepository.getAttachmentsForContext(context)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _attachmentActionError = MutableStateFlow<String?>(null)
    val attachmentActionError: StateFlow<String?> = _attachmentActionError.asStateFlow()

    fun clearAttachmentError() {
        _attachmentActionError.value = null
    }

    fun addChatAttachments(uris: List<android.net.Uri>) {
        val topic = _activeTopic.value ?: return
        if (uris.isEmpty()) return

        val uid = authRepository.currentUser?.uid ?: "local_user"
        val attachmentContext = com.example.data.attachments.AttachmentContext(
            contextType = com.example.data.attachments.AttachmentContextType.CHAT_CONVERSATION,
            contextId = topic.id.toString(),
            ownerUid = uid
        )

        viewModelScope.launch {
            for (uri in uris) {
                val result = studyAttachmentRepository.addAttachmentFromUri(
                    attachmentContext = attachmentContext,
                    uri = uri
                )
                if (result is GeminiResult.Error) {
                    _attachmentActionError.value = result.message
                }
            }
        }
    }

    val allOwnerAttachments: StateFlow<List<com.example.data.attachments.StudyAttachmentEntity>> = authRepository.authStateFlow
        .flatMapLatest { user ->
            val uid = user?.uid ?: "local_user"
            studyAttachmentRepository.getAllAttachmentsForOwner(uid)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getAttachmentsForContextFlow(
        contextType: com.example.data.attachments.AttachmentContextType,
        contextId: String
    ): Flow<List<com.example.data.attachments.StudyAttachmentEntity>> {
        val uid = authRepository.currentUser?.uid ?: "local_user"
        val context = com.example.data.attachments.AttachmentContext(
            contextType = contextType,
            contextId = contextId,
            ownerUid = uid
        )
        return studyAttachmentRepository.getAttachmentsForContext(context)
    }

    fun addTopicAttachments(
        contextType: com.example.data.attachments.AttachmentContextType,
        contextId: String,
        uris: List<android.net.Uri>
    ) {
        if (uris.isEmpty()) return
        val uid = authRepository.currentUser?.uid ?: "local_user"
        val attachmentContext = com.example.data.attachments.AttachmentContext(
            contextType = contextType,
            contextId = contextId,
            ownerUid = uid
        )

        viewModelScope.launch {
            for (uri in uris) {
                val result = studyAttachmentRepository.addAttachmentFromUri(
                    attachmentContext = attachmentContext,
                    uri = uri
                )
                if (result is GeminiResult.Error) {
                    _attachmentActionError.value = result.message
                }
            }
        }
    }

    fun removeAttachmentFromChat(attachmentId: String) {
        viewModelScope.launch {
            studyAttachmentRepository.removeFromContext(attachmentId)
        }
    }

    fun deleteAttachmentPermanently(attachmentId: String) {
        viewModelScope.launch {
            studyAttachmentRepository.deletePermanently(attachmentId)
        }
    }

    fun retryAttachment(attachmentId: String) {
        viewModelScope.launch {
            val result = studyAttachmentRepository.retryAttachmentUpload(attachmentId)
            if (result is GeminiResult.Error) {
                _attachmentActionError.value = result.message
            }
        }
    }

    // --- ACADEMIC PROFILE STATE (PHASE C2.5) ---
    val academicProfileRepository = com.example.data.profile.AcademicProfileRepository(
        context = application,
        dao = dao,
        preferencesManager = preferencesManager,
        curriculumRepository = curriculumRepository
    )

    val academicProfile: StateFlow<com.example.data.profile.AcademicProfile?> = academicProfileRepository
        .academicProfileFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, academicProfileRepository.getProfileSync())

    val hasAcademicProfile: StateFlow<Boolean> = academicProfileRepository.academicProfileFlow
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, academicProfileRepository.hasProfile())

    fun saveAcademicProfile(department: CurriculumDepartment, semester: Int) {
        viewModelScope.launch {
            val profile = academicProfileRepository.saveProfile(department, semester)
            val initialCourse = academicProfileRepository.getInitialCourseForProfile(profile)
            if (initialCourse != null) {
                selectRoadmap(initialCourse.roadmapId)
            }
            _activeAcademicContext.value = academicProfileRepository.buildDefaultAcademicContext(profile)
            val mission = dailyMissionRepository.handleAcademicProfileChange(profile)
            _todayMission.value = mission
            refreshRoadmapPercentages()
        }
    }

    fun saveAcademicProfileDirect(profile: com.example.data.profile.AcademicProfile) {
        viewModelScope.launch {
            val saved = academicProfileRepository.saveProfile(profile)
            val initialCourse = academicProfileRepository.getInitialCourseForProfile(saved)
            if (initialCourse != null) {
                selectRoadmap(initialCourse.roadmapId)
            }
            _activeAcademicContext.value = academicProfileRepository.buildDefaultAcademicContext(saved)
            val mission = dailyMissionRepository.handleAcademicProfileChange(saved)
            _todayMission.value = mission
            refreshRoadmapPercentages()
        }
    }

    // --- DAILY MISSION STATE (PHASE C2) ---
    val dailyMissionRepository = com.example.data.mission.DailyMissionRepository(
        context = application,
        dao = dao,
        curriculumRepository = curriculumRepository
    )

    private val _todayMission = MutableStateFlow<com.example.data.mission.DailyMissionEntity?>(null)
    val todayMission: StateFlow<com.example.data.mission.DailyMissionEntity?> = _todayMission.asStateFlow()

    val recentDailyMissions: StateFlow<List<com.example.data.mission.DailyMissionEntity>> = dailyMissionRepository
        .getRecentMissions(14)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

            // Initialize from persistent Academic Profile if available
            val profile = academicProfileRepository.getProfile()
            if (profile != null) {
                val initialCourse = academicProfileRepository.getInitialCourseForProfile(profile)
                val roadmapId = initialCourse?.roadmapId ?: preferencesManager.activeRoadmapId.ifBlank { "curriculum_cse_aiml_CS101" }
                preferencesManager.activeRoadmapId = roadmapId
                selectRoadmap(roadmapId)
                _activeAcademicContext.value = academicProfileRepository.buildDefaultAcademicContext(profile)
            } else {
                // If no profile established yet, check if there's a valid saved roadmap
                val currentRoadmapId = preferencesManager.activeRoadmapId
                if (currentRoadmapId.isNotBlank() && currentRoadmapId.startsWith("curriculum_")) {
                    selectRoadmap(currentRoadmapId)
                }
            }
            refreshRoadmapPercentages()

            // Initialize daily quiz
            loadDailyQuiz()

            // Initialize daily mission (Phase C2 & C2.5)
            loadTodayMission()
        }
        testAiConnection()
    }

    fun loadTodayMission() {
        viewModelScope.launch {
            val profile = academicProfileRepository.getProfile()
            // First ensure today's mission is created if absent
            val todayMission = dailyMissionRepository.getOrCreateTodayMission(
                academicProfile = profile,
                activeAcademicContext = _activeAcademicContext.value,
                activeRoadmapId = _activeRoadmapId.value,
                activeTopicTitle = _activeTopic.value?.title
            )
            _todayMission.value = todayMission

            // Then observe any updates (e.g. task completion, focus timer updates)
            dailyMissionRepository.getTodayMissionFlow().collect { mission ->
                if (mission != null) {
                    _todayMission.value = mission
                }
            }
        }
    }

    fun toggleMissionTask(taskId: String) {
        val mission = _todayMission.value ?: return
        viewModelScope.launch {
            dailyMissionRepository.toggleTaskCompletion(mission.id, taskId)
        }
    }

    fun completeTodayMission() {
        val mission = _todayMission.value ?: return
        viewModelScope.launch {
            dailyMissionRepository.completeMission(mission.id)
        }
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
            val roadmapTitle = roadmapDetail?.title ?: "Academic Curriculum"
            val progressList = roadmapRepository.getTopicProgressOnce(roadmapId)
            val completedTitles = progressList.filter { it.status == "COMPLETED" }.map { it.nodeTitle }
            val weakConcepts = activeWeakConcepts.value.map { it.concept }

            val curriculumCourse = if (roadmapId.startsWith("curriculum_")) {
                roadmapRepository.curriculumRepository.getCourseByRoadmapId(roadmapId)
            } else null

            val curriculumContext = if (curriculumCourse != null) {
                """
ACADEMIC CURRICULUM DATABASE CONTEXT:
Department: ${curriculumCourse.departmentName}
Programme: ${curriculumCourse.programme} (Regulation: ${curriculumCourse.regulation})
Institution: JIS College of Engineering
Course: ${curriculumCourse.code} - ${curriculumCourse.title}
Semester: Semester ${curriculumCourse.semester} (Year ${curriculumCourse.year}) | Credits: ${curriculumCourse.credits}
Contact Hours: ${curriculumCourse.contact.ifBlank { "Prescribed" }} | Type: ${curriculumCourse.courseType}
Current Module: ${node.title}

OFFICIAL SYLLABUS CONTENT:
${node.description}

PREREQUISITES:
${curriculumCourse.prerequisites.ifBlank { "None specified" }}

COURSE OBJECTIVES:
${curriculumCourse.objectives.ifBlank { "Master foundational concepts according to regulation standards." }}

COURSE OUTCOMES:
${curriculumCourse.outcomes.ifBlank { "Demonstrate theoretical proficiency and problem-solving mastery." }}

PRESCRIBED TEXTBOOKS & REFERENCES:
${curriculumCourse.textbooks.ifBlank { curriculumCourse.referenceBooks.ifBlank { "Prescribed institutional materials." } }}

Completed Modules: ${if (completedTitles.isEmpty()) "None" else completedTitles.joinToString(", ")}
Pedagogical Rule: "THE SYLLABUS IS THE ROADMAP." Teach strictly in accordance with this syllabus.
                """.trimIndent()
            } else {
                """
Roadmap: $roadmapTitle
Topic: ${node.title}
Category: ${node.category}
Difficulty: ${node.difficulty ?: "Intermediate"}
Description: ${node.description}
Completed Prerequisites: ${if (completedTitles.isEmpty()) "None" else completedTitles.joinToString(", ")}
Known Weak Topics: ${if (weakConcepts.isEmpty()) "None" else weakConcepts.take(3).joinToString(", ")}
                """.trimIndent()
            }

            // 3. Create or select a dedicated Topic in Room for this learning session
            val sessionTitle = if (curriculumCourse != null) {
                "${curriculumCourse.code}: ${node.title}"
            } else {
                "${node.title} — Learning"
            }
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
                    goal = if (curriculumCourse != null) {
                        "Master ${node.title} from ${curriculumCourse.code} (${curriculumCourse.departmentName}, ${curriculumCourse.regulation})"
                    } else {
                        "Master ${node.title} from the $roadmapTitle roadmap"
                    },
                    roadmapJson = curriculumContext
                )
                val id = dao.insertTopic(newTopic)
                newTopic.copy(id = id)
            }

            val academic = if (curriculumCourse != null) {
                com.example.data.studytools.AcademicContext(
                    department = curriculumCourse.departmentName,
                    programme = curriculumCourse.programme,
                    regulation = curriculumCourse.regulation,
                    semester = curriculumCourse.semester,
                    courseCode = curriculumCourse.code,
                    courseName = curriculumCourse.title,
                    module = node.title,
                    topic = node.title,
                    officialSyllabusContent = node.description
                )
            } else {
                com.example.data.studytools.AcademicContext(
                    courseName = roadmapTitle,
                    module = node.category,
                    topic = node.title,
                    officialSyllabusContent = node.description
                )
            }
            _activeAcademicContext.value = academic

            selectTopic(targetTopic)
            setMode("LEARNING")

            // 4. Send initial prompt to trigger structured 7-step teaching session
            val initialPrompt = if (curriculumCourse != null) {
                "Hi Sage! I am studying the official curriculum for '${curriculumCourse.code}: ${curriculumCourse.title}' (${curriculumCourse.departmentName}, Regulation ${curriculumCourse.regulation}), specifically '${node.title}'. Please teach me this module step-by-step according to our official syllabus: explain the core principles, provide concrete engineering examples, and test my comprehension."
            } else {
                "Hi Sage! I'm starting the topic '${node.title}' in my $roadmapTitle roadmap. Please begin our structured learning session: explain the core concept, provide a concrete example, and then ask me a comprehension question."
            }
            sendMessage(initialPrompt)

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
            val curriculumCourse = if (roadmapId.startsWith("curriculum_")) {
                roadmapRepository.curriculumRepository.getCourseByRoadmapId(roadmapId)
            } else null

            val sessionTitle = if (curriculumCourse != null) {
                "${curriculumCourse.code} Q&A"
            } else {
                "${node.title} Q&A"
            }

            val contextSnippet = if (curriculumCourse != null) {
                "Department: ${curriculumCourse.departmentName} (${curriculumCourse.regulation}) | Course: ${curriculumCourse.code} - ${curriculumCourse.title} | Module: ${node.title} | ${node.description}"
            } else {
                "Roadmap: $roadmapTitle | Topic: ${node.title} | ${node.description}"
            }

            val existingTopic = allTopics.value.find { it.title == sessionTitle }
            val targetTopic = if (existingTopic != null) {
                existingTopic
            } else {
                val newTopic = TopicEntity(
                    title = sessionTitle,
                    mode = "NORMAL",
                    goal = "Deep dive into ${node.title}",
                    roadmapJson = contextSnippet
                )
                val id = dao.insertTopic(newTopic)
                newTopic.copy(id = id)
            }

            val academic = if (curriculumCourse != null) {
                com.example.data.studytools.AcademicContext(
                    department = curriculumCourse.departmentName,
                    programme = curriculumCourse.programme,
                    regulation = curriculumCourse.regulation,
                    semester = curriculumCourse.semester,
                    courseCode = curriculumCourse.code,
                    courseName = curriculumCourse.title,
                    module = node.title,
                    topic = node.title,
                    officialSyllabusContent = node.description
                )
            } else {
                com.example.data.studytools.AcademicContext(
                    courseName = roadmapTitle,
                    module = node.category,
                    topic = node.title,
                    officialSyllabusContent = node.description
                )
            }
            _activeAcademicContext.value = academic

            selectTopic(targetTopic)
            setMode("NORMAL")
            val prompt = if (curriculumCourse != null) {
                "Can you give me an academic briefing on '${node.title}' from '${curriculumCourse.code}: ${curriculumCourse.title}' (${curriculumCourse.departmentName}, ${curriculumCourse.regulation})? Please explain why it matters, key principles, and how to approach learning it effectively."
            } else {
                "Can you give me a high-level briefing on '${node.title}' from the $roadmapTitle curriculum: why it matters and how to approach learning it effectively?"
            }
            sendMessage(prompt)
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

    fun selectCurriculumTopic(
        course: com.example.data.curriculum.CurriculumCourse,
        module: com.example.data.curriculum.CurriculumModule,
        specificTopic: String? = null,
        onNavigateToChat: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val topicName = specificTopic?.ifBlank { null } ?: module.title.ifBlank { "${module.moduleNumber}: Core Syllabus" }
            val academicContext = com.example.data.studytools.AcademicContext(
                department = course.departmentName,
                programme = course.programme,
                regulation = course.regulation,
                semester = course.semester,
                courseCode = course.code,
                courseName = course.title,
                module = module.title,
                topic = topicName,
                officialSyllabusContent = module.content
            )
            _activeAcademicContext.value = academicContext
            selectRoadmap(course.roadmapId)

            val sessionTitle = "${course.code}: $topicName"
            val existingTopic = allTopics.value.find { it.title.equals(sessionTitle, ignoreCase = true) }
            val targetTopic = if (existingTopic != null) {
                existingTopic
            } else {
                val newTopic = TopicEntity(
                    title = sessionTitle,
                    mode = "NORMAL",
                    goal = "Study $topicName in ${course.code} (${course.departmentName})",
                    roadmapJson = "${course.code} | Module: ${module.moduleNumber} | $topicName"
                )
                val id = dao.insertTopic(newTopic)
                newTopic.copy(id = id)
            }
            selectTopic(targetTopic)
            onNavigateToChat()
        }
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

    private fun buildCurriculumContextOverview(topic: TopicEntity): String {
        val roadmap = _activeRoadmapDetail.value
        val progressList = activeRoadmapProgress.value
        val completed = progressList.filter { it.status == "COMPLETED" }.map { it.nodeTitle }
        val studied = progressList.filter { it.studiedInChat }.map { it.nodeTitle }
        val needsReview = progressList.filter { it.status == "NEEDS_REVIEW" }.map { it.nodeTitle }
        val nextNode = roadmap?.nodes?.find { node ->
            progressList.find { it.nodeId == node.id }?.status.let { it == null || it == "NOT_STARTED" }
        }

        val curriculumCourse = if (_activeRoadmapId.value.startsWith("curriculum_")) {
            roadmapRepository.curriculumRepository.getCourseByRoadmapId(_activeRoadmapId.value)
        } else null

        val academicCtx = _activeAcademicContext.value

        return buildString {
            if (curriculumCourse != null) {
                appendLine("OFFICIAL ACADEMIC CURRICULUM CONTEXT:")
                appendLine("Institution: JIS College of Engineering")
                appendLine("Department: ${curriculumCourse.departmentName}")
                appendLine("Programme: ${curriculumCourse.programme} (${curriculumCourse.regulation})")
                appendLine("Course: ${curriculumCourse.code} - ${curriculumCourse.title}")
                appendLine("Semester: Semester ${curriculumCourse.semester} (Year ${curriculumCourse.year}) | Credits: ${curriculumCourse.credits}")
                if (academicCtx?.module != null) appendLine("Active Module: ${academicCtx.module}")
                if (academicCtx?.topic != null) appendLine("Active Syllabus Topic: ${academicCtx.topic}")
                if (curriculumCourse.prerequisites.isNotBlank()) appendLine("Prerequisites: ${curriculumCourse.prerequisites}")
                if (curriculumCourse.objectives.isNotBlank()) appendLine("Course Objectives: ${curriculumCourse.objectives}")
                if (curriculumCourse.outcomes.isNotBlank()) appendLine("Course Outcomes: ${curriculumCourse.outcomes}")
                if (curriculumCourse.textbooks.isNotBlank()) appendLine("Prescribed Textbooks: ${curriculumCourse.textbooks}")
                appendLine("ACADEMIC DIRECTIVE: THE SYLLABUS IS THE ROADMAP. Ground all explanations and quiz questions strictly in the official curriculum.")
            } else {
                appendLine("Active Roadmap: ${roadmap?.title ?: "Full Stack Web Development"}")
                if (academicCtx?.topic != null) appendLine("Active Topic: ${academicCtx.topic}")
            }
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
    }

    fun sendMessage(text: String) {
        val topic = _activeTopic.value ?: return
        if (text.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val roadmapOverview = buildCurriculumContextOverview(topic)

                // Track chat study interaction if current topic corresponds to roadmap node
                val roadmap = _activeRoadmapDetail.value
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

                val activeAttIds = activeConversationAttachments.value
                    .filter { !it.isDeleted && (it.uploadStatus == "ATTACHED" || it.processingStatus == "COMPLETED") }
                    .map { it.attachmentId }
                val idToken = authRepository.getIdToken()

                repository.sendMessage(
                    topicId = topic.id,
                    userText = text,
                    currentMode = _currentMode.value,
                    curriculumContext = roadmapOverview,
                    attachmentIds = activeAttIds,
                    idToken = idToken
                )
                refreshRoadmapPercentages()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun editAndResendMessage(messageId: Long, newText: String) {
        val topic = _activeTopic.value ?: return
        if (newText.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val roadmapOverview = buildCurriculumContextOverview(topic)

                val activeAttIds = activeConversationAttachments.value
                    .filter { !it.isDeleted && (it.uploadStatus == "ATTACHED" || it.processingStatus == "COMPLETED") }
                    .map { it.attachmentId }
                val idToken = authRepository.getIdToken()

                repository.editAndResendMessage(
                    topicId = topic.id,
                    userMessageId = messageId,
                    newText = newText,
                    currentMode = _currentMode.value,
                    curriculumContext = roadmapOverview,
                    attachmentIds = activeAttIds,
                    idToken = idToken
                )
                refreshRoadmapPercentages()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun retryAiResponse(messageId: Long) {
        val topic = _activeTopic.value ?: return
        if (_isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val roadmapOverview = buildCurriculumContextOverview(topic)

                val activeAttIds = activeConversationAttachments.value
                    .filter { !it.isDeleted && (it.uploadStatus == "ATTACHED" || it.processingStatus == "COMPLETED") }
                    .map { it.attachmentId }
                val idToken = authRepository.getIdToken()

                repository.retryAiResponse(
                    topicId = topic.id,
                    assistantMessageId = messageId,
                    currentMode = _currentMode.value,
                    curriculumContext = roadmapOverview,
                    attachmentIds = activeAttIds,
                    idToken = idToken
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

    val allCurriculumDepartments: List<CurriculumDepartment> by lazy { curriculumRepository.getAllDepartments() }
    val curriculumSource: String = "CurriculumRepository (assets/curriculum/cse_aiml_r25_database.json)"
    val curriculumFilesCount: Int get() = allCurriculumDepartments.size
    val curriculumCoursesCount: Int get() = curriculumRepository.getCoursesForDepartment("cse_aiml").size

    fun getCurriculumCoursesForDepartment(deptId: String): List<CurriculumCourse> {
        return curriculumRepository.getCoursesForDepartment(deptId)
    }

    fun searchCurriculumCourses(query: String, deptFilter: String? = null): List<CurriculumCourse> {
        return curriculumRepository.searchCourses(query, deptFilter)
    }

    fun startCurriculumCourse(course: CurriculumCourse, onOpenRoadmap: (String) -> Unit) {
        val roadmapId = course.roadmapId
        selectRoadmap(roadmapId)
        onOpenRoadmap(roadmapId)
    }
}
