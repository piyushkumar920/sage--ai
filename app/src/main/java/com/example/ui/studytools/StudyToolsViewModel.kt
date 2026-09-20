package com.example.ui.studytools

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiResult
import com.example.data.curriculum.CurriculumCourse
import com.example.data.local.PreferencesManager
import com.example.data.local.SageDatabase
import com.example.data.studytools.SavedStudyToolEntity
import com.example.data.studytools.StudyFlashcard
import com.example.data.studytools.StudyFlashcardsData
import com.example.data.studytools.StudyFormulaSheetData
import com.example.data.studytools.StudyMindMapData
import com.example.data.studytools.StudyNotesData
import com.example.data.studytools.StudyRevisionSheetData
import com.example.data.studytools.StudySolutionData
import com.example.data.studytools.StudyToolType
import com.example.data.studytools.StudyToolsRepository
import com.example.util.NetworkMonitor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class StudyToolUiState {
    object Idle : StudyToolUiState()
    object Loading : StudyToolUiState()
    data class Error(val message: String) : StudyToolUiState()
    data class NotesReady(val notes: StudyNotesData, val savedId: String? = null) : StudyToolUiState()
    data class FlashcardsReady(val flashcards: StudyFlashcardsData, val savedId: String? = null) : StudyToolUiState()
    data class MindMapReady(val mindMap: StudyMindMapData, val savedId: String? = null) : StudyToolUiState()
    data class RevisionReady(val revision: StudyRevisionSheetData, val savedId: String? = null) : StudyToolUiState()
    data class FormulasReady(val formulas: StudyFormulaSheetData, val savedId: String? = null) : StudyToolUiState()
    data class SolutionReady(val solution: StudySolutionData, val savedId: String? = null) : StudyToolUiState()
}

class StudyToolsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val geminiClient = GeminiClient(
        backendUrlProvider = { preferencesManager.customBackendUrl.ifEmpty { null } }
    )
    private val database = SageDatabase.getInstance(application)
    private val repository = StudyToolsRepository(
        context = application,
        dao = database.sageDao(),
        geminiClient = geminiClient
    )
    private val networkMonitor = NetworkMonitor(application)

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, networkMonitor.isCurrentlyConnected())

    val savedTools: StateFlow<List<SavedStudyToolEntity>> = repository.allSavedTools
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow<StudyToolUiState>(StudyToolUiState.Idle)
    val uiState: StateFlow<StudyToolUiState> = _uiState.asStateFlow()

    private val _selectedTool = MutableStateFlow<StudyToolType?>(null)
    val selectedTool: StateFlow<StudyToolType?> = _selectedTool.asStateFlow()

    // Context from academic curriculum (if opened from curriculum topic)
    var contextTopic: String = ""
    var contextSubject: String = ""
    var contextSyllabus: String = ""
    var contextAcademic: com.example.data.studytools.AcademicContext? = null

    // Scan & Solve Image URI
    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri.asStateFlow()

    // Flashcard interaction state
    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    fun selectTool(tool: StudyToolType?) {
        _selectedTool.value = tool
        _uiState.value = StudyToolUiState.Idle
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
    }

    fun setCurriculumContext(
        topic: String,
        subject: String,
        syllabus: String = "",
        academicContext: com.example.data.studytools.AcademicContext? = null
    ) {
        contextTopic = topic
        contextSubject = subject
        contextSyllabus = syllabus
        contextAcademic = academicContext
    }

    fun setImageUri(uri: Uri?) {
        _capturedImageUri.value = uri
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun nextCard(total: Int) {
        if (total > 0 && _currentCardIndex.value < total - 1) {
            _currentCardIndex.value += 1
            _isCardFlipped.value = false
        }
    }

    fun prevCard() {
        if (_currentCardIndex.value > 0) {
            _currentCardIndex.value -= 1
            _isCardFlipped.value = false
        }
    }

    // ==========================================
    // GENERATION HANDLERS
    // ==========================================

    fun generateNotes(topic: String, subject: String, prompt: String = "") {
        viewModelScope.launch {
            _uiState.value = StudyToolUiState.Loading
            val effectiveTopic = topic.ifBlank { contextTopic }
            val effectiveSubject = subject.ifBlank { contextSubject }

            when (val res = repository.generateNotes(
                topic = effectiveTopic,
                subject = effectiveSubject,
                syllabusContext = contextSyllabus,
                prompt = prompt,
                academicContext = contextAcademic
            )) {
                is GeminiResult.Success -> {
                    val entity = SavedStudyToolEntity(
                        id = "notes_${UUID.randomUUID()}",
                        type = StudyToolType.GENERATE_NOTES.id,
                        title = res.data.title.ifBlank { effectiveTopic },
                        subject = effectiveSubject,
                        topic = effectiveTopic,
                        syllabusContext = contextSyllabus,
                        contentJson = moshi.adapter(StudyNotesData::class.java).toJson(res.data)
                    )
                    repository.saveStudyTool(entity)
                    _uiState.value = StudyToolUiState.NotesReady(res.data, entity.id)
                }
                is GeminiResult.Error -> {
                    val typedError = com.example.data.studytools.StudyToolError.from(res)
                    _uiState.value = StudyToolUiState.Error(typedError.userMessage)
                }
            }
        }
    }

    fun generateFlashcards(topic: String, subject: String, prompt: String = "") {
        viewModelScope.launch {
            _uiState.value = StudyToolUiState.Loading
            val effectiveTopic = topic.ifBlank { contextTopic }
            val effectiveSubject = subject.ifBlank { contextSubject }

            when (val res = repository.generateFlashcards(
                topic = effectiveTopic,
                subject = effectiveSubject,
                syllabusContext = contextSyllabus,
                prompt = prompt,
                academicContext = contextAcademic
            )) {
                is GeminiResult.Success -> {
                    val entity = SavedStudyToolEntity(
                        id = "flash_${UUID.randomUUID()}",
                        type = StudyToolType.FLASHCARDS.id,
                        title = res.data.title.ifBlank { effectiveTopic },
                        subject = effectiveSubject,
                        topic = effectiveTopic,
                        syllabusContext = contextSyllabus,
                        contentJson = moshi.adapter(StudyFlashcardsData::class.java).toJson(res.data)
                    )
                    repository.saveStudyTool(entity)
                    _currentCardIndex.value = 0
                    _isCardFlipped.value = false
                    _uiState.value = StudyToolUiState.FlashcardsReady(res.data, entity.id)
                }
                is GeminiResult.Error -> {
                    val typedError = com.example.data.studytools.StudyToolError.from(res)
                    _uiState.value = StudyToolUiState.Error(typedError.userMessage)
                }
            }
        }
    }

    fun generateMindMap(topic: String, subject: String, prompt: String = "") {
        viewModelScope.launch {
            _uiState.value = StudyToolUiState.Loading
            val effectiveTopic = topic.ifBlank { contextTopic }
            val effectiveSubject = subject.ifBlank { contextSubject }

            when (val res = repository.generateMindMap(
                topic = effectiveTopic,
                subject = effectiveSubject,
                syllabusContext = contextSyllabus,
                prompt = prompt,
                academicContext = contextAcademic
            )) {
                is GeminiResult.Success -> {
                    val entity = SavedStudyToolEntity(
                        id = "mindmap_${UUID.randomUUID()}",
                        type = StudyToolType.MIND_MAP.id,
                        title = res.data.title.ifBlank { effectiveTopic },
                        subject = effectiveSubject,
                        topic = effectiveTopic,
                        syllabusContext = contextSyllabus,
                        contentJson = moshi.adapter(StudyMindMapData::class.java).toJson(res.data)
                    )
                    repository.saveStudyTool(entity)
                    _uiState.value = StudyToolUiState.MindMapReady(res.data, entity.id)
                }
                is GeminiResult.Error -> {
                    val typedError = com.example.data.studytools.StudyToolError.from(res)
                    _uiState.value = StudyToolUiState.Error(typedError.userMessage)
                }
            }
        }
    }

    fun generateRevisionSheet(topic: String, subject: String, prompt: String = "") {
        viewModelScope.launch {
            _uiState.value = StudyToolUiState.Loading
            val effectiveTopic = topic.ifBlank { contextTopic }
            val effectiveSubject = subject.ifBlank { contextSubject }

            when (val res = repository.generateRevisionSheet(
                topic = effectiveTopic,
                subject = effectiveSubject,
                syllabusContext = contextSyllabus,
                prompt = prompt,
                academicContext = contextAcademic
            )) {
                is GeminiResult.Success -> {
                    val entity = SavedStudyToolEntity(
                        id = "revision_${UUID.randomUUID()}",
                        type = StudyToolType.REVISION_SHEET.id,
                        title = res.data.title.ifBlank { effectiveTopic },
                        subject = effectiveSubject,
                        topic = effectiveTopic,
                        syllabusContext = contextSyllabus,
                        contentJson = moshi.adapter(StudyRevisionSheetData::class.java).toJson(res.data)
                    )
                    repository.saveStudyTool(entity)
                    _uiState.value = StudyToolUiState.RevisionReady(res.data, entity.id)
                }
                is GeminiResult.Error -> {
                    val typedError = com.example.data.studytools.StudyToolError.from(res)
                    _uiState.value = StudyToolUiState.Error(typedError.userMessage)
                }
            }
        }
    }

    fun generateFormulaSheet(topic: String, subject: String, prompt: String = "") {
        viewModelScope.launch {
            _uiState.value = StudyToolUiState.Loading
            val effectiveTopic = topic.ifBlank { contextTopic }
            val effectiveSubject = subject.ifBlank { contextSubject }

            when (val res = repository.generateFormulaSheet(
                topic = effectiveTopic,
                subject = effectiveSubject,
                syllabusContext = contextSyllabus,
                prompt = prompt,
                academicContext = contextAcademic
            )) {
                is GeminiResult.Success -> {
                    val entity = SavedStudyToolEntity(
                        id = "formulas_${UUID.randomUUID()}",
                        type = StudyToolType.FORMULA_SHEET.id,
                        title = res.data.title.ifBlank { effectiveTopic },
                        subject = effectiveSubject,
                        topic = effectiveTopic,
                        syllabusContext = contextSyllabus,
                        contentJson = moshi.adapter(StudyFormulaSheetData::class.java).toJson(res.data)
                    )
                    repository.saveStudyTool(entity)
                    _uiState.value = StudyToolUiState.FormulasReady(res.data, entity.id)
                }
                is GeminiResult.Error -> {
                    val typedError = com.example.data.studytools.StudyToolError.from(res)
                    _uiState.value = StudyToolUiState.Error(typedError.userMessage)
                }
            }
        }
    }

    fun solveImage(uri: Uri, prompt: String = "") {
        viewModelScope.launch {
            _uiState.value = StudyToolUiState.Loading
            when (val res = repository.solveImage(uri, prompt)) {
                is GeminiResult.Success -> {
                    val entity = SavedStudyToolEntity(
                        id = "solve_${UUID.randomUUID()}",
                        type = StudyToolType.SCAN_AND_SOLVE.id,
                        title = if (res.data.problem.length > 50) res.data.problem.take(50) + "..." else res.data.problem.ifBlank { "Solved Question" },
                        subject = res.data.subject,
                        topic = "Visual Problem Solution",
                        syllabusContext = "",
                        contentJson = moshi.adapter(StudySolutionData::class.java).toJson(res.data)
                    )
                    repository.saveStudyTool(entity)
                    _uiState.value = StudyToolUiState.SolutionReady(res.data, entity.id)
                }
                is GeminiResult.Error -> {
                    val typedError = com.example.data.studytools.StudyToolError.from(res)
                    _uiState.value = StudyToolUiState.Error(typedError.userMessage)
                }
            }
        }
    }

    fun loadSavedTool(savedTool: SavedStudyToolEntity) {
        when (savedTool.type) {
            StudyToolType.GENERATE_NOTES.id -> {
                val data = moshi.adapter(StudyNotesData::class.java).fromJson(savedTool.contentJson)
                if (data != null) {
                    _selectedTool.value = StudyToolType.GENERATE_NOTES
                    _uiState.value = StudyToolUiState.NotesReady(data, savedTool.id)
                }
            }
            StudyToolType.FLASHCARDS.id -> {
                val data = moshi.adapter(StudyFlashcardsData::class.java).fromJson(savedTool.contentJson)
                if (data != null) {
                    _selectedTool.value = StudyToolType.FLASHCARDS
                    _currentCardIndex.value = 0
                    _isCardFlipped.value = false
                    _uiState.value = StudyToolUiState.FlashcardsReady(data, savedTool.id)
                }
            }
            StudyToolType.MIND_MAP.id -> {
                val data = moshi.adapter(StudyMindMapData::class.java).fromJson(savedTool.contentJson)
                if (data != null) {
                    _selectedTool.value = StudyToolType.MIND_MAP
                    _uiState.value = StudyToolUiState.MindMapReady(data, savedTool.id)
                }
            }
            StudyToolType.REVISION_SHEET.id -> {
                val data = moshi.adapter(StudyRevisionSheetData::class.java).fromJson(savedTool.contentJson)
                if (data != null) {
                    _selectedTool.value = StudyToolType.REVISION_SHEET
                    _uiState.value = StudyToolUiState.RevisionReady(data, savedTool.id)
                }
            }
            StudyToolType.FORMULA_SHEET.id -> {
                val data = moshi.adapter(StudyFormulaSheetData::class.java).fromJson(savedTool.contentJson)
                if (data != null) {
                    _selectedTool.value = StudyToolType.FORMULA_SHEET
                    _uiState.value = StudyToolUiState.FormulasReady(data, savedTool.id)
                }
            }
            StudyToolType.SCAN_AND_SOLVE.id -> {
                val data = moshi.adapter(StudySolutionData::class.java).fromJson(savedTool.contentJson)
                if (data != null) {
                    _selectedTool.value = StudyToolType.SCAN_AND_SOLVE
                    _uiState.value = StudyToolUiState.SolutionReady(data, savedTool.id)
                }
            }
        }
    }

    fun deleteSavedTool(id: String) {
        viewModelScope.launch {
            repository.deleteStudyTool(id)
            if (_uiState.value is StudyToolUiState.NotesReady && (_uiState.value as StudyToolUiState.NotesReady).savedId == id) {
                _uiState.value = StudyToolUiState.Idle
            }
        }
    }

    fun resetState() {
        _uiState.value = StudyToolUiState.Idle
    }
}
