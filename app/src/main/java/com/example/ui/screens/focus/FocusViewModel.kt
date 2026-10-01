package com.example.ui.screens.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.curriculum.CurriculumRepository
import com.example.data.focus.FocusRepository
import com.example.data.focus.FocusStats
import com.example.data.local.FocusSessionEntity
import com.example.data.local.SageDatabase
import com.example.data.studytools.AcademicContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FocusScreenState {
    SETUP,
    ACTIVE,
    SUMMARY
}

enum class StuckAction {
    EXPLAIN_CONCEPT,
    GIVE_HINT,
    SIMPLER_EXAMPLE,
    SOLVE_STEP_BY_STEP,
    SCAN_QUESTION
}

class FocusViewModel(
    application: Application,
    private val focusRepository: FocusRepository,
    private val curriculumRepository: CurriculumRepository,
    private val dailyMissionRepository: com.example.data.mission.DailyMissionRepository = com.example.data.mission.DailyMissionRepository(
        application,
        SageDatabase.getInstance(application).sageDao(),
        curriculumRepository
    )
) : AndroidViewModel(application) {

    private val _screenState = MutableStateFlow(FocusScreenState.SETUP)
    val screenState: StateFlow<FocusScreenState> = _screenState.asStateFlow()

    // Daily Mission integration (Phase C2)
    private val _isDailyMissionSession = MutableStateFlow(false)
    val isDailyMissionSession: StateFlow<Boolean> = _isDailyMissionSession.asStateFlow()

    private val _activeMissionId = MutableStateFlow<String?>(null)
    val activeMissionId: StateFlow<String?> = _activeMissionId.asStateFlow()

    private val _missionTasks = MutableStateFlow<List<com.example.data.mission.MissionTask>>(emptyList())
    val missionTasks: StateFlow<List<com.example.data.mission.MissionTask>> = _missionTasks.asStateFlow()

    // Setup state
    private val _academicContext = MutableStateFlow<AcademicContext?>(null)
    val academicContext: StateFlow<AcademicContext?> = _academicContext.asStateFlow()

    private val _plannedMinutes = MutableStateFlow(25)
    val plannedMinutes: StateFlow<Int> = _plannedMinutes.asStateFlow()

    private val _goal = MutableStateFlow("")
    val goal: StateFlow<String> = _goal.asStateFlow()

    // Active session timer & checkpoints
    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _actualFocusedSeconds = MutableStateFlow(0)
    val actualFocusedSeconds: StateFlow<Int> = _actualFocusedSeconds.asStateFlow()

    private val _learnCompleted = MutableStateFlow(false)
    val learnCompleted: StateFlow<Boolean> = _learnCompleted.asStateFlow()

    private val _practiceCompleted = MutableStateFlow(false)
    val practiceCompleted: StateFlow<Boolean> = _practiceCompleted.asStateFlow()

    private val _reviewCompleted = MutableStateFlow(false)
    val reviewCompleted: StateFlow<Boolean> = _reviewCompleted.asStateFlow()

    private val _sessionStartTimeMs = MutableStateFlow(0L)

    private var timerJob: Job? = null

    val focusStats: StateFlow<FocusStats> = focusRepository.focusStats.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FocusStats()
    )

    fun initializeWithContext(context: AcademicContext?) {
        if (_screenState.value == FocusScreenState.ACTIVE) {
            // Do not overwrite an active in-progress session
            return
        }

        _isDailyMissionSession.value = false
        _activeMissionId.value = null
        _missionTasks.value = emptyList()

        val resolvedContext = context ?: resolveDefaultCurriculumContext()
        _academicContext.value = resolvedContext
        if (_goal.value.isBlank() || _goal.value == FocusRepository.deriveDefaultGoal(null)) {
            _goal.value = FocusRepository.deriveDefaultGoal(resolvedContext)
        }
        _screenState.value = FocusScreenState.SETUP
    }

    /**
     * Initializes Focus Mode for an adaptive weekly review session targeting a weak concept.
     */
    fun initializeForReviewSession(concept: String, context: AcademicContext?) {
        if (_screenState.value == FocusScreenState.ACTIVE) return

        _isDailyMissionSession.value = false
        _activeMissionId.value = null
        _missionTasks.value = emptyList()

        val baseContext = context ?: resolveDefaultCurriculumContext()
        val reviewContext = baseContext.copy(topic = concept)
        _academicContext.value = reviewContext
        _goal.value = "Weekly Review: Review $concept and solve one core practice problem."
        _plannedMinutes.value = 20
        _remainingSeconds.value = 20 * 60
        _actualFocusedSeconds.value = 0
        _learnCompleted.value = false
        _practiceCompleted.value = false
        _reviewCompleted.value = false
        _screenState.value = FocusScreenState.SETUP
    }

    fun initializeWithMission(mission: com.example.data.mission.DailyMissionEntity) {
        if (_screenState.value == FocusScreenState.ACTIVE) return

        _isDailyMissionSession.value = true
        _activeMissionId.value = mission.id
        _academicContext.value = mission.toAcademicContext()
        _goal.value = mission.goal
        _plannedMinutes.value = mission.estimatedMinutes
        _remainingSeconds.value = mission.estimatedMinutes * 60
        _missionTasks.value = mission.parseTasks()
        _screenState.value = FocusScreenState.SETUP
    }

    fun startFiveMinuteMissionFocus(mission: com.example.data.mission.DailyMissionEntity) {
        _isDailyMissionSession.value = true
        _activeMissionId.value = mission.id
        _academicContext.value = mission.toAcademicContext()
        _goal.value = "5-Minute Focus: ${mission.topic}"
        _plannedMinutes.value = 5
        _remainingSeconds.value = 5 * 60
        _missionTasks.value = mission.parseTasks()
        _actualFocusedSeconds.value = 0
        _learnCompleted.value = false
        _practiceCompleted.value = false
        _reviewCompleted.value = false
        _sessionStartTimeMs.value = System.currentTimeMillis()
        _screenState.value = FocusScreenState.ACTIVE
        startTimer()
    }

    fun toggleMissionTask(taskId: String) {
        val missionId = _activeMissionId.value ?: return
        viewModelScope.launch {
            dailyMissionRepository.toggleTaskCompletion(missionId, taskId)
            val updatedTasks = _missionTasks.value.map {
                if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
            }
            _missionTasks.value = updatedTasks
        }
    }

    private fun resolveDefaultCurriculumContext(): AcademicContext {
        val dept = curriculumRepository.getAllDepartments().firstOrNull()
        val courses = if (dept != null) curriculumRepository.getCoursesForDepartment(dept.id) else emptyList()
        val course = courses.firstOrNull()
        val module = course?.modules?.firstOrNull()

        return AcademicContext(
            department = dept?.shortName ?: "CSE (AI & ML)",
            programme = dept?.programme ?: "B. Tech CSE (AI & ML)",
            regulation = dept?.regulation ?: "R25",
            semester = course?.semester ?: 1,
            courseCode = course?.code ?: "CS101",
            courseName = course?.title ?: "Introduction to Programming",
            module = module?.moduleNumber ?: "Module 1",
            topic = module?.title ?: "Problem Solving & Algorithms"
        )
    }

    fun setAcademicContext(context: AcademicContext) {
        _academicContext.value = context
        _goal.value = FocusRepository.deriveDefaultGoal(context)
    }

    fun setPlannedMinutes(minutes: Int) {
        _plannedMinutes.value = minutes
        if (_screenState.value == FocusScreenState.SETUP) {
            _remainingSeconds.value = minutes * 60
        }
    }

    fun setGoal(customGoal: String) {
        _goal.value = customGoal
    }

    fun startFocusSession() {
        _remainingSeconds.value = _plannedMinutes.value * 60
        _actualFocusedSeconds.value = 0
        _learnCompleted.value = false
        _practiceCompleted.value = false
        _reviewCompleted.value = false
        _sessionStartTimeMs.value = System.currentTimeMillis()
        _screenState.value = FocusScreenState.ACTIVE
        startTimer()
    }

    fun togglePlayPause() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _remainingSeconds.value > 0) {
                delay(1000)
                if (_isTimerRunning.value) {
                    _remainingSeconds.value = (_remainingSeconds.value - 1).coerceAtLeast(0)
                    _actualFocusedSeconds.value += 1

                    if (_remainingSeconds.value <= 0) {
                        finishSession(completedNormally = true)
                    }
                }
            }
        }
    }

    fun toggleLearnCheckpoint() {
        _learnCompleted.value = !_learnCompleted.value
    }

    fun togglePracticeCheckpoint() {
        _practiceCompleted.value = !_practiceCompleted.value
    }

    fun toggleReviewCheckpoint() {
        _reviewCompleted.value = !_reviewCompleted.value
    }

    fun finishSession(completedNormally: Boolean = false) {
        pauseTimer()
        _screenState.value = FocusScreenState.SUMMARY

        // Persist session to local Room database
        viewModelScope.launch {
            val ctx = _academicContext.value
            val entity = FocusSessionEntity(
                startedAt = if (_sessionStartTimeMs.value > 0) _sessionStartTimeMs.value else System.currentTimeMillis(),
                endedAt = System.currentTimeMillis(),
                plannedDurationMinutes = _plannedMinutes.value,
                actualFocusedSeconds = _actualFocusedSeconds.value,
                department = ctx?.department ?: "",
                programme = ctx?.programme ?: "B. Tech",
                regulation = ctx?.regulation ?: "R25",
                semester = ctx?.semester,
                courseCode = ctx?.courseCode ?: "",
                courseName = ctx?.courseName ?: "",
                module = ctx?.module ?: "",
                topic = ctx?.topic ?: "",
                goal = _goal.value,
                learnCompleted = _learnCompleted.value,
                practiceCompleted = _practiceCompleted.value,
                reviewCompleted = _reviewCompleted.value,
                completedNormally = completedNormally
            )
            focusRepository.recordFocusSession(entity)

            // If this was a Daily Mission focus session, record the focused minutes
            if (_isDailyMissionSession.value && _activeMissionId.value != null) {
                dailyMissionRepository.recordMissionFocusTime(
                    missionId = _activeMissionId.value!!,
                    seconds = _actualFocusedSeconds.value
                )
            }
        }
    }

    fun resetToSetup() {
        pauseTimer()
        _remainingSeconds.value = _plannedMinutes.value * 60
        _actualFocusedSeconds.value = 0
        _learnCompleted.value = false
        _practiceCompleted.value = false
        _reviewCompleted.value = false
        _screenState.value = FocusScreenState.SETUP
    }

    fun getStuckPrompt(action: StuckAction): String {
        val topic = _academicContext.value?.topic?.ifBlank { null }
            ?: _academicContext.value?.courseName?.ifBlank { null }
            ?: "this concept"

        return when (action) {
            StuckAction.EXPLAIN_CONCEPT ->
                "Hi Sage! I'm doing a focused study session on '$topic'. Can you explain this concept step-by-step from the fundamentals with an intuitive analogy?"
            StuckAction.GIVE_HINT ->
                "Hi Sage! I'm stuck on '$topic'. Can you give me a progressive hint to guide my thinking without giving away the full answer immediately?"
            StuckAction.SIMPLER_EXAMPLE ->
                "Hi Sage! Can you illustrate '$topic' using a simpler, real-world example suitable for exam recall?"
            StuckAction.SOLVE_STEP_BY_STEP ->
                "Hi Sage! Walk me through a standard question and step-by-step numerical/conceptual solution for '$topic'."
            StuckAction.SCAN_QUESTION ->
                "Let's scan and solve a question from '$topic'."
        }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = SageDatabase.getInstance(application)
            val focusRepo = FocusRepository(db.sageDao())
            val curriculumRepo = CurriculumRepository(application)
            return FocusViewModel(application, focusRepo, curriculumRepo) as T
        }
    }
}
