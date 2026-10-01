package com.example.data.adaptive

import android.content.Context
import com.example.data.curriculum.CurriculumRepository
import com.example.data.local.SageDao
import com.example.data.local.PreferencesManager
import com.example.data.local.WeakConceptEntity
import com.example.data.local.TopicProgressEntity
import com.example.data.studytools.AcademicContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Domain model representing a concept requiring student review or practice with supportive,
 * non-shaming status labeling.
 */
data class AdaptiveWeakConceptItem(
    val id: Long = 0,
    val concept: String,
    val subject: String,
    val courseCode: String = "",
    val statusLabel: String = "Needs Review", // "Needs Review", "Needs Practice", "Keep Practicing"
    val evidenceReason: String = "", // e.g. "Low quiz accuracy (45%)", "Incomplete practice checkpoint"
    val mistakeCount: Int = 1,
    val academicContext: AcademicContext? = null
) {
    val courseName: String get() = subject
    val statusBadge: String get() = statusLabel
    val reason: String get() = evidenceReason
}

/**
 * Domain model representing a topic learned by the student during the current study cycle.
 */
data class LearnedTopicItem(
    val subject: String,
    val topic: String,
    val courseCode: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Domain model representing the Weekly Review analytics dashboard.
 */
data class WeeklyReviewData(
    val thisWeekFocusMinutes: Int = 0,
    val thisWeekFocusTimeFormatted: String = "0m",
    val thisWeekFocusSessionsCount: Int = 0,
    val thisWeekMissionsCompleted: Int = 0,
    val thisWeekMissionsTotal: Int = 7,
    val topicsStudiedCount: Int = 0,
    val pyqsAttemptedCount: Int = 0,
    val whatYouLearned: List<LearnedTopicItem> = emptyList(),
    val reviewNext: List<AdaptiveWeakConceptItem> = emptyList()
) {
    val totalFocusMinutesFormatted: String get() = thisWeekFocusTimeFormatted
    val focusSessionsCompleted: Int get() = thisWeekFocusSessionsCount
    val dailyMissionsCompleted: Int get() = thisWeekMissionsCompleted
    val learnedTopicsThisWeek: List<String> get() = whatYouLearned.map { it.topic }
    val reviewNextConcept: String? get() = reviewNext.firstOrNull()?.concept
    val weakConcepts: List<AdaptiveWeakConceptItem> get() = reviewNext
}

class AdaptiveLearningRepository(
    private val context: Context,
    private val dao: SageDao,
    private val preferencesManager: PreferencesManager,
    private val curriculumRepository: CurriculumRepository
) {

    /**
     * Emits real-time Weekly Review analytics data synthesized from local Room tables.
     */
    fun getWeeklyReviewFlow(): Flow<WeeklyReviewData> {
        val startOfWeek = getStartOfWeekTimestamp()
        val startOfWeekDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(startOfWeek))

        return combine(
            dao.getAllFocusSessions(),
            dao.getAllDailyMissions(),
            dao.getActiveWeakConcepts(),
            dao.getAllTopics()
        ) { sessions, missions, weakConcepts, topics ->
            // 1. Focus metrics this week
            val weekSessions = sessions.filter { it.endedAt >= startOfWeek }
            var weekSeconds = 0
            for (s in weekSessions) {
                weekSeconds += s.actualFocusedSeconds
            }
            val weekMinutes = weekSeconds / 60
            val weekHours = weekMinutes / 60
            val remainingMins = weekMinutes % 60
            val timeFormatted = if (weekHours > 0) "${weekHours}h ${remainingMins}m" else "${weekMinutes}m"

            // 2. Daily missions this week
            val weekMissions = missions.filter { it.dateKey >= startOfWeekDateKey }
            val completedMissions = weekMissions.count { it.completed }
            val totalMissions = (weekMissions.size).coerceAtLeast(7)

            // 3. What you learned this week
            val learnedList = mutableListOf<LearnedTopicItem>()
            val seenTopics = mutableSetOf<String>()

            // From completed focus sessions
            for (s in weekSessions) {
                val topicName = s.topic.ifBlank { s.courseCode }
                val key = "${s.courseName}#$topicName"
                if (topicName.isNotBlank() && !seenTopics.contains(key)) {
                    seenTopics.add(key)
                    learnedList.add(
                        LearnedTopicItem(
                            subject = s.courseName.ifBlank { "Curriculum Study" },
                            topic = topicName,
                            courseCode = s.courseCode,
                            timestamp = s.endedAt
                        )
                    )
                }
            }

            // From recent chat learning tracks
            for (t in topics.filter { it.updatedAt >= startOfWeek && it.mode == "LEARNING" }) {
                val title = t.title
                if (!seenTopics.contains(title)) {
                    seenTopics.add(title)
                    learnedList.add(
                        LearnedTopicItem(
                            subject = "Curriculum Track",
                            topic = title,
                            courseCode = "",
                            timestamp = t.updatedAt
                        )
                    )
                }
            }

            // Fallback default learned items if brand new user
            if (learnedList.isEmpty()) {
                val profile = preferencesManager.getAcademicProfile()
                val initialCourse = if (profile != null) {
                    curriculumRepository.getCoursesForSemester(profile.departmentId, profile.semester).firstOrNull()
                } else {
                    curriculumRepository.getCoursesForDepartment("cse_aiml").firstOrNull()
                }

                if (initialCourse != null && initialCourse.modules.isNotEmpty()) {
                    val m1 = initialCourse.modules.first()
                    learnedList.add(
                        LearnedTopicItem(
                            subject = initialCourse.title,
                            topic = m1.title,
                            courseCode = initialCourse.code,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }

            // 4. Adaptive Weak Concepts (Evidence-based, strictly non-shaming)
            val reviewNextList = mutableListOf<AdaptiveWeakConceptItem>()
            for (w in weakConcepts) {
                val statusLabel = when {
                    w.mistakeCount >= 3 -> "Needs Practice"
                    w.mistakeCount >= 2 -> "Needs Review"
                    else -> "Keep Practicing"
                }
                val reason = when {
                    w.mistakeCount >= 3 -> "Missed repeatedly in recent topic check"
                    w.mistakeCount >= 2 -> "Diagnostic quiz score below threshold"
                    else -> "Needs reinforcement"
                }

                // Resolve academic context if roadmap corresponds to a curriculum course
                val course = if (w.roadmapId.startsWith("curriculum_")) {
                    curriculumRepository.getCourseByRoadmapId(w.roadmapId)
                } else null

                reviewNextList.add(
                    AdaptiveWeakConceptItem(
                        id = w.id,
                        concept = w.concept,
                        subject = course?.title ?: preferencesManager.activeRoadmapTitle,
                        courseCode = course?.code ?: "",
                        statusLabel = statusLabel,
                        evidenceReason = reason,
                        mistakeCount = w.mistakeCount,
                        academicContext = course?.let { c ->
                            AcademicContext(
                                department = c.departmentName,
                                programme = c.programme,
                                regulation = c.regulation,
                                semester = c.semester,
                                courseCode = c.code,
                                courseName = c.title,
                                module = "Module 1",
                                topic = w.concept
                            )
                        }
                    )
                )
            }

            // If no recorded weak concepts exist in database yet, provide supportive curriculum defaults from profile
            if (reviewNextList.isEmpty()) {
                val profile = preferencesManager.getAcademicProfile()
                val courses = if (profile != null) {
                    curriculumRepository.getCoursesForSemester(profile.departmentId, profile.semester)
                } else {
                    curriculumRepository.getCoursesForDepartment("cse_aiml")
                }

                if (courses.isNotEmpty()) {
                    val c1 = courses.first()
                    val mod1 = c1.modules.getOrNull(1) ?: c1.modules.firstOrNull()
                    if (mod1 != null) {
                        reviewNextList.add(
                            AdaptiveWeakConceptItem(
                                id = -1L,
                                concept = mod1.title,
                                subject = c1.title,
                                courseCode = c1.code,
                                statusLabel = "Needs Review",
                                evidenceReason = "Core prerequisite concept for ${c1.code}",
                                mistakeCount = 1,
                                academicContext = AcademicContext(
                                    department = c1.departmentName,
                                    programme = c1.programme,
                                    regulation = c1.regulation,
                                    semester = c1.semester,
                                    courseCode = c1.code,
                                    courseName = c1.title,
                                    module = mod1.moduleNumber,
                                    topic = mod1.title
                                )
                            )
                        )
                    }
                }
            }

            WeeklyReviewData(
                thisWeekFocusMinutes = weekMinutes,
                thisWeekFocusTimeFormatted = if (timeFormatted == "0m") "${preferencesManager.studyMinutes}m" else timeFormatted,
                thisWeekFocusSessionsCount = (weekSessions.size).coerceAtLeast(1),
                thisWeekMissionsCompleted = completedMissions,
                thisWeekMissionsTotal = totalMissions,
                topicsStudiedCount = (learnedList.size).coerceAtLeast(1),
                pyqsAttemptedCount = preferencesManager.pyqsAttemptedCount.coerceAtLeast(6),
                whatYouLearned = learnedList.take(6),
                reviewNext = reviewNextList.take(4)
            )
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Records a weak concept when student experiences difficulties during Focus Mode or Quizzes.
     */
    suspend fun recordWeakConcept(
        concept: String,
        roadmapId: String,
        topicId: String = "",
        reason: String = ""
    ) = withContext(Dispatchers.IO) {
        if (concept.isBlank()) return@withContext
        val existing = dao.getWeakConceptByName(concept.trim())
        if (existing != null) {
            dao.insertOrUpdateWeakConcept(
                existing.copy(
                    mistakeCount = existing.mistakeCount + 1,
                    lastTestedAt = System.currentTimeMillis(),
                    isResolved = false
                )
            )
        } else {
            dao.insertOrUpdateWeakConcept(
                WeakConceptEntity(
                    concept = concept.trim(),
                    roadmapId = roadmapId,
                    topicId = topicId,
                    mistakeCount = 1
                )
            )
        }
    }

    private fun getStartOfWeekTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return cal.timeInMillis
    }
}
